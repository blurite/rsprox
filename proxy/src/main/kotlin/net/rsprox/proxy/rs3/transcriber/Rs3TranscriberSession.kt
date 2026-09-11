package net.rsprox.proxy.rs3.transcriber

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionTracker

public class Rs3TranscriberSession(
    private val runner: Rs3TranscriberRunner,
    private val sessionTracker: Rs3SessionTracker,
    public val sessionState: Rs3SessionState,
) {
    public fun onClientProt(prot: ClientProt, message: IncomingMessage) {
        sessionTracker.onClientPacket(message, prot)
        sessionTracker.beforeTranscribe(message)
        runner.onClientProt(prot, message)
        sessionTracker.afterTranscribe(message)
    }

    public fun onServerPacket(prot: ClientProt, message: IncomingMessage) {
        sessionTracker.onServerPacket(message, prot)
        sessionTracker.beforeTranscribe(message)
        runner.onServerPacket(prot, message)
        sessionTracker.afterTranscribe(message)
    }
}
