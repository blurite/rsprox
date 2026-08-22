package net.rsprox.protocol.rs3v949.game.incoming.model.unknown

import net.rsprot.protocol.message.IncomingMessage

public class RawUnknownClientPacket(
    public val opcode: Int,
    public val name: String,
    public val bytes: ByteArray,
) : IncomingMessage {
    override fun toString(): String = "RawUnknownClientPacket(opcode=$opcode, name=$name, ${bytes.size}b)"
}
