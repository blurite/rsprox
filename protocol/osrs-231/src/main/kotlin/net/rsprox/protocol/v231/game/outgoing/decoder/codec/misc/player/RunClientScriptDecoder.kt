package net.rsprox.protocol.v231.game.outgoing.decoder.codec.misc.player

import io.netty.buffer.ByteBuf
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.g1
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class RunClientScriptDecoder : ProxyMessageDecoder<RunClientScript> {
    override val prot: ClientProt = GameServerProt.RUNCLIENTSCRIPT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RunClientScript {
        val types = buffer.gjstr()
        val arguments = ArrayDeque<Any>()
        for (char in types.reversed()) {
            when (char) {
                'W' -> {
                    val length = buffer.buffer.gVarInt2()
                    val array =
                        IntArray(length) {
                            buffer.buffer.gVarInt2s()
                        }
                    arguments.addFirst(array)
                }
                'X' -> {
                    val length = buffer.buffer.gVarInt2()
                    val array =
                        Array(length) {
                            buffer.buffer.gjstr()
                        }
                    arguments.addFirst(array)
                }
                's' -> {
                    arguments.addFirst(buffer.gjstr())
                }
                else -> {
                    arguments.addFirst(buffer.g4())
                }
            }
        }
        val id = buffer.g4()
        return RunClientScript(
            id,
            types.toCharArray(),
            arguments,
        )
    }

    // TODO: Once RSProt 231 is published, update the dependency and switch over to that.
    private fun ByteBuf.gVarInt2(): Int {
        var value = 0
        var bits = 0
        do {
            val temp = g1()
            value = value or ((temp and 0x7F) shl bits)
            bits += Byte.SIZE_BITS - 1
        } while (temp > 0x7F)
        return value
    }

    private fun ByteBuf.gVarInt2s(): Int {
        val unsigned = gVarInt2()
        return (unsigned ushr 1) xor -(unsigned and 0x1)
    }
}
