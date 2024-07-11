// package net.rsprox.protocol.game.incoming.decoder.codec.misc.client
//
// import net.rsprot.buffer.JagByteBuf
// import net.rsprot.protocol.ClientProt
// import net.rsprot.protocol.message.codec.MessageDecoder
// import net.rsprot.protocol.metadata.Consistent
// import net.rsprot.protocol.tools.MessageDecodingTools
// import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
// import net.rsprox.protocol.game.incoming.model.misc.client.ReflectionCheckReply
//
// @Consistent
// public class ReflectionCheckReplyDecoder : ProxyMessageDecoder<ReflectionCheckReply> {
//    override val prot: ClientProt = GameClientProt.REFLECTION_CHECK_REPLY
//
//    override fun decode(
//        buffer: JagByteBuf,
//        tools: MessageDecodingTools,
//    ): ReflectionCheckReply {
//        val copy = buffer.buffer.copy()
//        val id = buffer.g4()
//        // Mark the buffer as "read" as copy function doesn't do it automatically.
//        buffer.buffer.readerIndex(buffer.buffer.writerIndex())
//        return ReflectionCheckReply(
//            id,
//            copy,
//        )
//    }
// }
