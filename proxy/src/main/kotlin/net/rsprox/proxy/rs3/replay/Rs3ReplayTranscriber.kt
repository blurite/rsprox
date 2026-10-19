package net.rsprox.proxy.rs3.replay

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.clientscript.RSProxArchiveClientScriptIndex
import net.rsprox.cache.rs3.Rs3LiveCacheResolver
import net.rsprox.protocol.rs3.cache.rs3PacketDefinitions
import net.rsprox.protocol.rs3.game.incoming.model.unknown.RawUnknownClientPacket
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3AppearanceDefinitions
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.rs3PlayerInfoInitPending
import net.rsprox.protocol.rs3.game.outgoing.model.unknown.RawUnknownServerPacket
import net.rsprox.protocol.session.AttributeMap
import net.rsprox.protocol.session.Session
import net.rsprox.proxy.replay.ReplayTranscriber
import net.rsprox.proxy.replay.ReplayTranscript
import net.rsprox.proxy.rs3.Rs3ProtocolRevision
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.MessageConsumer
import net.rsprox.transcriber.rs3.text.Rs3PropertyFormatter
import net.rsprox.transcriber.rs3.text.TextRs3TranscriberProvider
import net.rsprox.transcriber.text.TextMessageConsumerContainer

internal object Rs3ReplayTranscriber {
    fun transcribe(
        replay: Rs3ReplaySession,
        filters: PropertyFilterSetStore,
        settings: SettingSetStore,
    ): ReplayTranscript {
        val recording = replay.recording
        val header = recording.header
        val revision = Rs3ProtocolRevision(header.revision).wireRevision
        val definitions = Rs3LiveCacheResolver.loadRecordedPacketDefinitions(revision, header.js5MasterIndex)
        val gamevals = Rs3GamevalLookup(definitions.gamevals)
        val scripts = RSProxArchiveClientScriptIndex.forRuneScape(revision, header.js5MasterIndex).also { it.preload() }
        val monitor =
            ReplayTranscriber.TranscriptMonitor(
                Rs3PropertyFormatter.create(settings, gamevals, scripts),
            )
        var tick = 0
        val consumer =
            object : MessageConsumer {
                override fun consume(
                    formatter: PropertyTreeFormatter,
                    cycle: Int,
                    property: RootProperty,
                ) {
                    monitor.onTranscribe(tick, property)
                }

                override fun close() = Unit
            }
        val transcriber =
            TextRs3TranscriberProvider(gamevals)
                .provide(TextMessageConsumerContainer(listOf(consumer)), filters, settings, scripts, isLobby = false)
        transcriber.sessionState.localPlayerIndex = header.localPlayerIndex

        fun session(): Session =
            Session(header.localPlayerIndex, AttributeMap()).apply {
                rs3PacketDefinitions = definitions
                rs3AppearanceDefinitions = definitions
            }
        val client = session()
        val server = session().apply { rs3PlayerInfoInitPending = true }
        var failures = 0
        for (frame in recording.game) {
            tick = frame.tick
            val prot = frame.prot as ClientProt
            val outgoing = frame.direction == StreamDirection.SERVER_TO_CLIENT
            val buffer = Unpooled.wrappedBuffer(frame.payload)
            try {
                val message =
                    try {
                        if (outgoing) {
                            recording.decoder.serverPacketDecoder.decode(prot.opcode, buffer.toJagByteBuf(), server)
                        } else {
                            recording.decoder.clientPacketDecoder.decode(prot.opcode, buffer.toJagByteBuf(), client)
                        }
                    } catch (error: Exception) {
                        failures++
                        val reason = "${error.javaClass.simpleName}: ${error.message.orEmpty()}"
                        if (outgoing) {
                            RawUnknownServerPacket(prot.opcode, prot.toString(), frame.payload, reason)
                        } else {
                            RawUnknownClientPacket(prot.opcode, prot.toString(), frame.payload, reason)
                        }
                    }
                if (outgoing) transcriber.onServerPacket(prot, message) else transcriber.onClientProt(prot, message)
            } finally {
                buffer.release()
            }
        }
        return ReplayTranscript(monitor.entries, failures)
    }
}
