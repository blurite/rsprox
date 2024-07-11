package net.rsprox.protocol

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage

public class MessageDecoderRepository<out P : ClientProt> internal constructor(
    private val protRepository: ProtRepository<P>,
    private val decoders: Array<ProxyMessageDecoder<*>?>,
    private val decoderToMessageClassMap:
        Map<Class<out ProxyMessageDecoder<IncomingMessage>>, Class<out IncomingMessage>>,
) {
    public fun getDecoder(opcode: Int): ProxyMessageDecoder<*> {
        return decoders[opcode]
            ?: throw IllegalArgumentException("Opcode $opcode is not registered.")
    }

    public fun getMessageClass(
        decoderClazz: Class<out ProxyMessageDecoder<IncomingMessage>>,
    ): Class<out IncomingMessage> {
        return requireNotNull(decoderToMessageClassMap[decoderClazz]) {
            "Message class does not exist for $decoderClazz"
        }
    }

    public fun getSize(opcode: Int): Int {
        return protRepository.getSize(opcode)
    }
}
