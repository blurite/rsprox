package net.rsprox.transcriber.base

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.OmitFilteredPropertyFormatter
import net.rsprox.transcriber.ClientPacketTranscriber
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.ServerPacketTranscriber
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.state.StateTracker

public class BaseTranscriber private constructor(
    private val container: MessageConsumerContainer,
    formatter: BaseMessageFormatter,
    private val stateTracker: StateTracker,
    cacheProvider: CacheProvider,
    override val monitor: SessionMonitor<*>,
) : Transcriber,
    ClientPacketTranscriber by BaseClientPacketTranscriber(stateTracker),
    ServerPacketTranscriber by BaseServerPacketTranscriber(
        formatter,
        container,
        stateTracker,
        cacheProvider.get(),
        monitor,
    ) {
    public constructor(
        container: MessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        stateTracker: StateTracker,
    ) : this(
        container,
        BaseMessageFormatter(),
        stateTracker,
        cacheProvider,
        monitor,
    )

    override val cache: Cache = cacheProvider.get()

    override fun setCurrentProt(prot: Prot) {
        stateTracker.currentProt = prot
    }

    override fun onTranscribeStart() {
        stateTracker.setRoot()
    }

    override fun onTranscribeEnd() {
        val formatter = OmitFilteredPropertyFormatter()
        val lines = formatter.format(stateTracker.root)
        for (line in lines) {
            println(line)
        }
        /*val hasGroups = stateTracker.root.children.any { it is GroupProperty }
        if (!hasGroups) {
            // If there were no groups defined, we add an implicit group around all the properties
            val children = stateTracker.root.children.toList()
            stateTracker.root.children.clear()
            stateTracker.root.group {
                this.children.addAll(children)
            }
        }*/
    }
}
