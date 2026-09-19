package net.rsprox.transcriber.rs3

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.transcriber.rs3.state.Rs3SessionState
import net.rsprox.transcriber.rs3.state.Rs3SessionTracker

public class Rs3TranscriberSession(
    private val runner: Rs3TranscriberRunner,
    private val sessionTracker: Rs3SessionTracker,
    public val sessionState: Rs3SessionState,
) {
    public fun onClientProt(prot: ClientProt, message: IncomingMessage) {
        sessionTracker.onClientPacket(message, prot)
        sessionTracker.beforeTranscribe(message)
        try {
            runner.onClientProt(prot, message)
        } finally {
            sessionTracker.afterTranscribe(message)
        }
    }

    public fun onServerPacket(prot: ClientProt, message: IncomingMessage) {
        sessionTracker.onServerPacket(message, prot)
        sessionTracker.beforeTranscribe(message)
        try {
            runner.onServerPacket(prot, message)
        } finally {
            sessionTracker.afterTranscribe(message)
        }
    }
}
