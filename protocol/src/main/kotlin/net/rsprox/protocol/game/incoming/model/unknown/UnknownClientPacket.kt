package net.rsprox.protocol.game.incoming.model.unknown

public interface UnknownClientPacket {
    public val opcode: Int
    public val name: String
    public val bytes: ByteArray
}
