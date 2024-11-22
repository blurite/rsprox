package net.rsprox.protocol.v227.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class EventKeyboardDecoder : ProxyMessageDecoder<EventKeyboard> {
    override val prot: ClientProt = GameClientProt.EVENT_KEYBOARD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventKeyboard {
        val count = buffer.readableBytes() / 4
        val keys = ByteArray(count)
        var lastTransmittedKeyPress: Int = -1
        for (i in 0..<count) {
            val key = buffer.g1()
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
