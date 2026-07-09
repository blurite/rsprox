package net.rsprox.proxy.replay

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.p2
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.Prot
import net.rsprox.cache.Js5MasterIndex
import net.rsprox.cache.OldSchoolCache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.resolver.CacheResolver
import net.rsprox.cache.store.OpenRs2DiskCacheStore
import net.rsprox.cache.util.CacheGroupRequest
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getBytesConsumed
import net.rsprox.protocol.session.getRemainingBytesInPacketGroup
import net.rsprox.protocol.session.setBytesConsumed
import net.rsprox.protocol.session.setRemainingBytesInPacketGroup
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.plugin.DecoderLoader
import net.rsprox.proxy.plugin.DirectionalPacket
import net.rsprox.proxy.plugin.RevisionDecoder
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.indexing.NopBinaryIndex
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.isExcluded
import net.rsprox.shared.property.regular.GroupProperty
import net.rsprox.shared.property.regular.ListProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.shared.symbols.SymbolDictionaryProvider
import net.rsprox.transcriber.Packet
import net.rsprox.transcriber.TranscriberRunner
import net.rsprox.transcriber.state.SessionState
import net.rsprox.transcriber.state.SessionTracker
import net.rsprox.transcriber.text.TextMessageConsumerContainer
import net.rsprox.transcriber.text.TextTranscriberProvider

public data class ReplayTranscript(
    public val entries: List<ReplayTranscriptEntry>,
    public val failures: Int,
)

public data class ReplayTranscriptEntry(
    public val tick: Int,
    public val node: ReplayTranscriptNode,
)

public data class ReplayTranscriptNode(
    public val protocol: String?,
    public val content: String?,
    public val children: List<ReplayTranscriptNode> = emptyList(),
)

public class ReplayTranscriber(
    private val decoderLoader: DecoderLoader,
    private val filters: PropertyFilterSetStore,
    private val settings: SettingSetStore,
) {
    public fun transcribe(session: ReplaySession): ReplayTranscript {
        val cacheProvider = createReplayCacheProvider(session)
        val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.default(
                    SymbolDictionaryProvider.get(),
                    settings,
                    cacheProvider,
                ),
            )
        val monitor = TranscriptMonitor(formatter)
        val revisionDecoder = loadReplayDecoder(session, cacheProvider)
        val sessionState = SessionState(session.timeline.header.revision, settings)
        val runner =
            TextTranscriberProvider().provide(
                TextMessageConsumerContainer(emptyList()),
                cacheProvider,
                monitor,
                filters,
                settings,
                NopBinaryIndex,
                sessionState,
            )
        val tracker = SessionTracker(sessionState, cacheProvider.get(), monitor)
        val protocolSession = Session(session.timeline.header.localPlayerIndex, AttributeMap())
        var failures = 0
        val packetList = mutableListOf<Packet>()
        for (frame in session.timeline.frames) {
            try {
                val packets =
                    decodeReplayFrame(frame, revisionDecoder, protocolSession)
                        .map { Packet(StreamDirection.SERVER_TO_CLIENT, it.prot, it.message) }
                packetList += packets
                if (packets.any { it.prot.toString() == "SERVER_TICK_END" }) {
                    executeRunner(runner, tracker, packetList, session.timeline.header.revision)
                    packetList.clear()
                }
            } catch (_: NotImplementedError) {
                failures++
            } catch (_: Throwable) {
                failures++
            }
        }
        if (packetList.isNotEmpty()) {
            executeRunner(runner, tracker, packetList, session.timeline.header.revision)
        }
        return ReplayTranscript(monitor.entries, failures)
    }

    private fun executeRunner(
        runner: TranscriberRunner,
        tracker: SessionTracker,
        packets: List<Packet>,
        revision: Int,
    ) {
        for (packet in runner.preprocess(packets)) {
            tracker.onServerPacket(packet.message, packet.prot)
            tracker.beforeTranscribe(packet.message)
            runner.onServerPacket(packet.prot, packet.message, revision)
            tracker.afterTranscribe(packet.message)
        }
    }

    private fun decodeReplayFrame(
        frame: ReplayFrame,
        revisionDecoder: RevisionDecoder,
        protocolSession: Session,
    ): List<DirectionalPacket> {
        val prot = frame.prot
        var read = frame.payload.size
        read += if (prot.opcode < 128) 1 else 2
        if (prot.size == Prot.VAR_BYTE) {
            read++
        } else if (prot.size == Prot.VAR_SHORT) {
            read += 2
        }
        val payload = Unpooled.wrappedBuffer(frame.payload).toJagByteBuf()
        val remainingBytesInPacketGroup = protocolSession.getRemainingBytesInPacketGroup()
        val packet = revisionDecoder.decodeServerPacket(prot.opcode, payload, protocolSession)
        if (remainingBytesInPacketGroup != null && remainingBytesInPacketGroup > 0) {
            protocolSession.setBytesConsumed((protocolSession.getBytesConsumed() ?: 0) + read)
            if (remainingBytesInPacketGroup - read <= 0) {
                protocolSession.setRemainingBytesInPacketGroup(null)
                val outBuf = Unpooled.buffer(2).toJagByteBuf()
                outBuf.p2(protocolSession.getBytesConsumed() ?: 0)
                protocolSession.setBytesConsumed(null)
                return listOf(
                    DirectionalPacket(StreamDirection.SERVER_TO_CLIENT, prot, packet),
                    DirectionalPacket(
                        StreamDirection.SERVER_TO_CLIENT,
                        revisionDecoder.gameServerProtProvider[0xFE],
                        revisionDecoder.decodeServerPacket(0xFE, outBuf, protocolSession),
                    ),
                )
            }
            protocolSession.setRemainingBytesInPacketGroup(remainingBytesInPacketGroup - read)
        }
        return listOf(DirectionalPacket(StreamDirection.SERVER_TO_CLIENT, prot, packet))
    }

    private fun createReplayCacheProvider(session: ReplaySession): CacheProvider {
        val masterIndex =
            Js5MasterIndex.trimmed(
                session.timeline.header.revision,
                session.timeline.header.js5MasterIndex,
            )
        val cache = OldSchoolCache(GroupStoreCacheResolver(session), masterIndex)
        return CacheProvider { cache }
    }

    private fun loadReplayDecoder(
        session: ReplaySession,
        cacheProvider: CacheProvider,
    ): RevisionDecoder {
        decoderLoader.load(cacheProvider, session.timeline.header.revision)
        return decoderLoader.getDecoder(session.timeline.header.revision, cacheProvider)
    }

    private class TranscriptMonitor(
        private val formatter: OmitFilteredPropertyTreeFormatter,
    ) : SessionMonitor<BinaryHeader> {
        val entries: MutableList<ReplayTranscriptEntry> = mutableListOf()

        override fun onLogin(header: BinaryHeader) {
        }

        override fun onLogout(header: BinaryHeader) {
        }

        override fun onCacheUpdate(cacheProvider: CacheProvider) {
        }

        override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        }

        override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        }

        override fun onNameUpdate(name: String) {
        }

        override fun onUserInformationUpdate(
            userId: Long,
            userHash: Long,
        ) {
        }

        override fun onTranscribe(
            cycle: Int,
            property: RootProperty,
        ) {
            entries += ReplayTranscriptEntry(cycle, createRootNode(property))
        }

        private fun createRootNode(property: RootProperty): ReplayTranscriptNode {
            return ReplayTranscriptNode(property.prot, getPreviewText(property, 0), createChildNodes(property, 0))
        }

        private fun createNode(
            property: Property,
            protocol: String?,
            indent: Int,
        ): ReplayTranscriptNode {
            val children = createChildNodes(property, indent + 1)
            return ReplayTranscriptNode(protocol, getPreviewText(property, indent), children)
        }

        private fun createChildNodes(
            property: Property,
            indent: Int,
        ): List<ReplayTranscriptNode> {
            return buildList {
                for (child in property.children) {
                    if (child.isExcluded()) continue
                    if (child.children.isEmpty()) continue
                    when (child) {
                        is GroupProperty -> add(createNode(child, child.propertyName, indent))
                        is ListProperty -> add(createListNode(child, indent))
                        else -> error("Unsupported property with children. Property type: ${child::class.simpleName}")
                    }
                }
            }
        }

        private fun createListNode(
            property: ListProperty,
            indent: Int,
        ): ReplayTranscriptNode {
            return ReplayTranscriptNode(property.propertyName, getPreviewText(property, indent))
        }

        private fun getPreviewText(
            property: Property,
            indent: Int,
        ): String {
            val previewProps = property.children.filter { it.children.isEmpty() }
            if (previewProps.isEmpty()) {
                return ""
            }
            val lines = mutableListOf<String>()
            val builder = StringBuilder()
            var count = 0
            for (child: ChildProperty<*> in previewProps) {
                if (child.isExcluded()) {
                    continue
                }
                val linePrefix = if (count++ == 0) null else ", "
                formatter.writeChild(child, builder, lines, indent, linePrefix)
            }
            lines.add(builder.toString())
            return lines.joinToString(separator = System.lineSeparator())
        }
    }

    private class GroupStoreCacheResolver(
        private val session: ReplaySession,
    ) : CacheResolver {
        override fun get(
            masterIndex: Js5MasterIndex,
            archive: Int,
            group: Int,
        ): ByteBuf? = session.cacheStore.get(archive, group)

        override fun getBulk(
            masterIndex: Js5MasterIndex,
            requests: List<CacheGroupRequest>,
        ): Map<CacheGroupRequest, ByteBuf> {
            val store = session.cacheStore
            if (store is OpenRs2DiskCacheStore) {
                return store
                    .getBulk(requests.map { it.archive to it.group })
                    .mapKeys { (request, _) -> CacheGroupRequest(request.first, request.second) }
            }
            return buildMap {
                for (request in requests) {
                    val buffer = store.get(request.archive, request.group) ?: continue
                    put(request, buffer)
                }
            }
        }
    }
}
