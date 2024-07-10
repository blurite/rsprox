package net.rsprox.proxy.plugin

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.ServerPacketDecoder
import net.rsprox.proxy.huffman.HuffmanProvider

public data class DecoderPlugin(
    private val clientPacketDecoder: ClientPacketDecoder,
    private val serverPacketDecoder: ServerPacketDecoder,
    public val gameClientProtProvider: ProtProvider<ClientProt>,
    public val gameServerProtProvider: ProtProvider<ClientProt>,
) {
    private val tools = MessageDecodingTools(DefaultHuffmanCodecProvider(HuffmanProvider.get()))

    public fun decodeClientPacket(
        opcode: Int,
        buffer: JagByteBuf,
    ): IncomingMessage {
        return clientPacketDecoder.decode(
            opcode,
            buffer,
            tools,
        )
    }

    public fun decodeServerPacket(
        opcode: Int,
        buffer: JagByteBuf,
    ): IncomingMessage {
        return serverPacketDecoder.decode(
            opcode,
            buffer,
            tools,
        )
    }
}
