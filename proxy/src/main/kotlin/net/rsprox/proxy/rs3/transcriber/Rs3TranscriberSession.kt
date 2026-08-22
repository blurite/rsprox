package net.rsprox.proxy.rs3.transcriber

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionTracker

public class Rs3TranscriberSession(
    private val runner: Rs3TranscriberRunner,
    private val sessionTracker: Rs3SessionTracker,
    public val sessionState: Rs3SessionState,
) {
    public fun onClientProt(
        prot: GameClientProt,
        message: IncomingMessage,
    ) {
        sessionTracker.onClientPacket(message, prot)
        sessionTracker.beforeTranscribe(message)
        runner.onClientProt(prot, message)
        sessionTracker.afterTranscribe(message)
    }

    public fun onServerPacket(
        prot: GameServerProt,
        message: IncomingMessage,
    ) {
        sessionTracker.onServerPacket(message, prot)
        sessionTracker.beforeTranscribe(message)
        runner.onServerPacket(prot, message)
        sessionTracker.afterTranscribe(message)
    }
}
