package net.rsprox.transcriber.rs3

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage

public interface Rs3TranscriberRunner {
    public fun onClientProt(prot: ClientProt, message: IncomingMessage)

    public fun onServerPacket(prot: ClientProt, message: IncomingMessage)
}
