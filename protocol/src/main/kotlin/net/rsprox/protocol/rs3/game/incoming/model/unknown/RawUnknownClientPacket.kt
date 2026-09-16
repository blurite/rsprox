package net.rsprox.protocol.rs3.game.incoming.model.unknown

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.incoming.model.unknown.UnknownClientPacket

public class RawUnknownClientPacket(
    override val opcode: Int,
    override val name: String,
    override val bytes: ByteArray,
    public val decodeFailure: String? = null,
) : IncomingMessage, UnknownClientPacket {
    override fun toString(): String = "RawUnknownClientPacket(opcode=$opcode, name=$name, ${bytes.size}b)"
}
