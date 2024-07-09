package net.rsprox.protocol.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.events.EventKeyboard

public class EventKeyboardDecoder : MessageDecoder<EventKeyboard> {
    override val prot: ClientProt = GameClientProt.EVENT_KEYBOARD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): EventKeyboard {
        val count = buffer.readableBytes() / 4
        val keys = ByteArray(count)
        var lastTransmittedKeyPress: Int = -1
        for (i in 0..<count) {
            val key = buffer.g1Alt3()
            val delta = buffer.g3()
            if (i == 0) {
                lastTransmittedKeyPress = delta
            }
            keys[i] = key.toByte()
        }
        return EventKeyboard(
            lastTransmittedKeyPress,
            EventKeyboard.KeySequence(keys),
        )
    }
}
