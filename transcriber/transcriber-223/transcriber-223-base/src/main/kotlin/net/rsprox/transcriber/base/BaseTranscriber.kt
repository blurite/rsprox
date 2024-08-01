package net.rsprox.transcriber.base

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.base.impl.BaseClientPacketTranscriber
import net.rsprox.transcriber.base.impl.BaseNpcInfoTranscriber
import net.rsprox.transcriber.base.impl.BasePlayerInfoTranscriber
import net.rsprox.transcriber.base.impl.BaseServerPacketTranscriber
import net.rsprox.transcriber.impl.ClientPacketTranscriber
import net.rsprox.transcriber.impl.NpcInfoTranscriber
import net.rsprox.transcriber.impl.PlayerInfoTranscriber
import net.rsprox.transcriber.impl.ServerPacketTranscriber
import net.rsprox.transcriber.state.StateTracker

public class BaseTranscriber private constructor(
    private val stateTracker: StateTracker,
    cacheProvider: CacheProvider,
    override val monitor: SessionMonitor<*>,
    private val consumers: MessageConsumerContainer,
    private val formatter: PropertyTreeFormatter,
    private val filterSetStore: PropertyFilterSetStore,
) : Transcriber,
    ClientPacketTranscriber by BaseClientPacketTranscriber(
        stateTracker,
        filterSetStore,
    ),
    ServerPacketTranscriber by BaseServerPacketTranscriber(
        stateTracker,
        cacheProvider.get(),
        filterSetStore,
    ),
    PlayerInfoTranscriber by BasePlayerInfoTranscriber(
        stateTracker,
        monitor,
        filterSetStore,
    ),
    NpcInfoTranscriber by BaseNpcInfoTranscriber(
        stateTracker,
        cacheProvider.get(),
        filterSetStore,
    ) {
    public constructor(
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        stateTracker: StateTracker,
        consumers: MessageConsumerContainer,
        formatter: PropertyTreeFormatter,
        filters: PropertyFilterSetStore,
    ) : this(
        stateTracker,
        cacheProvider,
        monitor,
        consumers,
        formatter,
        filters,
    )

    override val cache: Cache = cacheProvider.get()

    override fun setCurrentProt(prot: Prot) {
        stateTracker.currentProt = prot
    }

    override fun onTranscribeStart() {
        stateTracker.setRoot()
    }

    override fun onTranscribeEnd() {
        val root = stateTracker.root
        if (root.isEmpty()) return
        var cycle = stateTracker.cycle
        // Decrement the cycle if we're logging server tick end
        if (stateTracker.currentProt == GameServerProt.SERVER_TICK_END) {
            cycle--
        }
        for (property in root) {
            consumers.publish(formatter, cycle, property)
        }
        root.clear()
    }
}
