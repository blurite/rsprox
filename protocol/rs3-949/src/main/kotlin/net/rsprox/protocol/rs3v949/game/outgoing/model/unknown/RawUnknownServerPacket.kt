package net.rsprox.protocol.rs3v949.game.outgoing.model.unknown

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class RawUnknownServerPacket(
    public val opcode: Int,
    public val name: String,
    public val bytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String = "RawUnknownServerPacket(opcode=$opcode, name=$name, ${bytes.size}b)"
}
