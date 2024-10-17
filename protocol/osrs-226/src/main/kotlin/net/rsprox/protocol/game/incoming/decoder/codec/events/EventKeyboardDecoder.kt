package net.rsprox.protocol.game.incoming.decoder.codec.events
import net.rsprox.protocol.session.Session

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class EventKeyboardDecoder : ProxyMessageDecoder<EventKeyboard> {
    override val prot: ClientProt = GameClientProt.EVENT_KEYBOARD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventKeyboard {
        val count = buffer.readableBytes() / 4
        val keys = ByteArray(count)
        var lastTransmittedKeyPress: Int = -1
        for (i in 0..<count) {
            val delta = buffer.g3Alt2()
            val key = buffer.g1Alt1()
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
