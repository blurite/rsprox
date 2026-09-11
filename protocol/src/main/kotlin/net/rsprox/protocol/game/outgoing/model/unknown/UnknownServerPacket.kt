package net.rsprox.protocol.game.outgoing.model.unknown

public interface UnknownServerPacket {
    public val opcode: Int
    public val name: String
    public val bytes: ByteArray
}
