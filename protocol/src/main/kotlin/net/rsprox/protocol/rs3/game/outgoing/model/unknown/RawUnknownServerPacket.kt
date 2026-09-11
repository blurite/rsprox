package net.rsprox.protocol.rs3.game.outgoing.model.unknown

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownServerPacket

public class RawUnknownServerPacket(
    override val opcode: Int,
    override val name: String,
    override val bytes: ByteArray,
) : IncomingServerGameMessage, UnknownServerPacket {
    override fun toString(): String = "RawUnknownServerPacket(opcode=$opcode, name=$name, ${bytes.size}b)"
}
