package net.rsprox.proxy.rs3.transcriber

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt

public interface Rs3TranscriberRunner {
    public fun onClientProt(
        prot: GameClientProt,
        message: IncomingMessage,
    )

    public fun onServerPacket(
        prot: GameServerProt,
        message: IncomingMessage,
    )
}
