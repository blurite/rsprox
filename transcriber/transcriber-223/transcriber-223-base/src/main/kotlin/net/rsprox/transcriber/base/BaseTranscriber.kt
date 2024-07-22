package net.rsprox.transcriber.base

import net.rsprot.protocol.Prot
import net.rsprox.transcriber.ClientPacketTranscriber
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.ServerPacketTranscriber
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.state.StateTracker

public class BaseTranscriber private constructor(
    private val container: MessageConsumerContainer,
    formatter: BaseMessageFormatter,
    private val stateTracker: StateTracker,
) : Transcriber,
    ClientPacketTranscriber by BaseClientPacketTranscriber(formatter, container, stateTracker),
    ServerPacketTranscriber by BaseServerPacketTranscriber(formatter, container, stateTracker) {
    public constructor(container: MessageConsumerContainer) :
        this(container, BaseMessageFormatter(), StateTracker())

    override fun setCurrentProt(prot: Prot) {
        stateTracker.currentProt = prot
    }
}
