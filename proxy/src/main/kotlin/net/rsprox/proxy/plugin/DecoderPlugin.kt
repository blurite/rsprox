package net.rsprox.proxy.plugin

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.protocol.session.Session

public data class DecoderPlugin(
    private val clientPacketDecoder: ClientPacketDecoder,
    private val serverPacketDecoder: ServerPacketDecoder,
    public val gameClientProtProvider: ProtProvider<ClientProt>,
    public val gameServerProtProvider: ProtProvider<ClientProt>,
) {
    public fun decodeClientPacket(
        opcode: Int,
        buffer: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        return clientPacketDecoder.decode(
            opcode,
            buffer,
            session,
        )
    }

    public fun decodeServerPacket(
        opcode: Int,
        buffer: JagByteBuf,
        session: Session,
    ): IncomingMessage {
        return serverPacketDecoder.decode(
            opcode,
            buffer,
            session,
        )
    }
}
