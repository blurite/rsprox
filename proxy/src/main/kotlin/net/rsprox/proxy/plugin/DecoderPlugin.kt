package net.rsprox.proxy.plugin

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.provider.DefaultHuffmanCodecProvider
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ServerProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.ClientPacketDecoder
import net.rsprox.protocol.ProtProvider
import net.rsprox.proxy.huffman.HuffmanProvider

public data class DecoderPlugin(
    public val instance: ClientPacketDecoder,
    public val gameClientProtProvider: ProtProvider<ClientProt>,
    public val gameServerProtProvider: ProtProvider<ServerProt>,
) {
    private val tools = MessageDecodingTools(DefaultHuffmanCodecProvider(HuffmanProvider.get()))

    public fun decode(
        opcode: Int,
        buffer: JagByteBuf,
    ): IncomingMessage {
        return instance.decode(
            opcode,
            buffer,
            tools,
        )
    }
}
