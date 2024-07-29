package net.rsprox.transcriber.base

import net.rsprot.protocol.Prot
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.shared.SessionMonitor
import net.rsprox.transcriber.ClientPacketTranscriber
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.NpcInfoTranscriber
import net.rsprox.transcriber.PlayerInfoTranscriber
import net.rsprox.transcriber.ServerPacketTranscriber
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.state.StateTracker

public class BaseTranscriber private constructor(
    private val stateTracker: StateTracker,
    cacheProvider: CacheProvider,
    override val monitor: SessionMonitor<*>,
    private val consumers: MessageConsumerContainer,
) : Transcriber,
    ClientPacketTranscriber by BaseClientPacketTranscriber(stateTracker),
    ServerPacketTranscriber by BaseServerPacketTranscriber(
        stateTracker,
        cacheProvider.get(),
    ),
    PlayerInfoTranscriber by BasePlayerInfoTranscriber(
        stateTracker,
        monitor,
    ),
    NpcInfoTranscriber by BaseNpcInfoTranscriber(
        stateTracker,
        cacheProvider.get(),
    ) {
    public constructor(
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        stateTracker: StateTracker,
        consumers: MessageConsumerContainer,
    ) : this(
        stateTracker,
        cacheProvider,
        monitor,
        consumers,
    )

    override val cache: Cache = cacheProvider.get()

    override fun setCurrentProt(prot: Prot) {
        stateTracker.currentProt = prot
    }

    override fun onTranscribeStart() {
        stateTracker.setRoot()
    }

    override fun onTranscribeEnd() {
        var cycle = stateTracker.cycle
        // Decrement the cycle if we're logging server tick end
        if (stateTracker.currentProt == GameServerProt.SERVER_TICK_END) {
            cycle--
        }
        consumers.publish(cycle, stateTracker.root)
    }
}
