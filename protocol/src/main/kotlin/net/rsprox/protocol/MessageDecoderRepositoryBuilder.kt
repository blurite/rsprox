package net.rsprox.protocol

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.ProtRepository
import net.rsprot.protocol.message.IncomingMessage

public class MessageDecoderRepositoryBuilder<P : ClientProt>(
    private val protRepository: ProtRepository<P>,
) {
    private val decoders: Array<ProxyMessageDecoder<*>?> = arrayOfNulls(protRepository.capacity())
    private val decoderClassToMessageClassMap:
        MutableMap<Class<out ProxyMessageDecoder<IncomingMessage>>, Class<out IncomingMessage>> = hashMapOf()

    public inline fun <reified T : IncomingMessage> bind(decoder: ProxyMessageDecoder<T>) {
        bind(T::class.java, decoder)
    }

    public fun <T : IncomingMessage> bind(
        messageClass: Class<T>,
        decoder: ProxyMessageDecoder<T>,
    ) {
        val clientProt = decoder.prot
        requireNotNull(decoders[clientProt.opcode] == null) {
            "Decoder for $messageClass is already bound."
        }
        decoders[clientProt.opcode] = decoder
        decoderClassToMessageClassMap[decoder::class.java] = messageClass
    }

    public fun build(): MessageDecoderRepository<P> {
        return MessageDecoderRepository(
            protRepository,
            decoders.copyOf(),
            decoderClassToMessageClassMap.toMap(),
        )
    }
}
