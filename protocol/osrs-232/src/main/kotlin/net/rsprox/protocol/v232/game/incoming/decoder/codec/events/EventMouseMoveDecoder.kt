package net.rsprox.protocol.v232.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.game.incoming.model.events.util.MouseMovements
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt

@Suppress("DuplicatedCode")
@Consistent
public class EventMouseMoveDecoder : ProxyMessageDecoder<EventMouseMove> {
    override val prot: ClientProt = GameClientProt.EVENT_MOUSE_MOVE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): EventMouseMove {
        val stepExcess = buffer.g1()
        val endExcess = buffer.g1()
        val array = threadLocalArray.get()
        var count = 0
        while (buffer.isReadable) {
            var packed = buffer.g1()
            var x: Int
            var y: Int
            var timeSinceLastMovement: Int
            var delta: Boolean
            if (packed and 0xE0 == 0xE0) {
                timeSinceLastMovement = packed and 0x1f shl 8 or buffer.g1()
                y = buffer.g2s()
                x = buffer.g2s()
                delta = false
                if (x == 0 && y == -0x8000) {
                    x = -1
                    y = -1
                }
            } else if (packed and 0xC0 == 0xC0) {
                timeSinceLastMovement = packed and 0x3f
                y = buffer.g2s()
                x = buffer.g2s()
                delta = false
                if (x == 0 && y == -0x8000) {
                    x = -1
                    y = -1
                }
            } else if (packed and 0x80 == 0x80) {
                timeSinceLastMovement = packed and 0x7f
                x = buffer.g1() - 128
                y = buffer.g1() - 128
                delta = true
            } else {
                packed = (packed shl 8) or (buffer.g1())
                timeSinceLastMovement = (packed ushr 12) and 0x7
                x = ((packed shr 6) and 0x3F) - 32
                y = (packed and 0x3F) - 32
                delta = true
            }
            val change =
                MouseMovements.MousePosChange.pack(
                    timeSinceLastMovement,
                    x,
                    y,
                    delta,
                )
            array[count++] = change
        }
        val slice = array.copyOf(count)
        return EventMouseMove(
            stepExcess,
            endExcess,
            MouseMovements(slice),
        )
    }

    private companion object {
        /**
         * Utilizing a thread-local initial long array, as the number of
         * mouse movements is unknown (relies on remaining bytes in buffer,
         * which in turn uses compression methods so each entry can be 2-4 bytes).
         * As Netty's threads decode this, a thread-local implementation is
         * perfectly safe to utilize, and will save us some memory in return.
         */
        private val threadLocalArray =
            ThreadLocal.withInitial {
                LongArray(128)
            }
    }
}
