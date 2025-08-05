package net.rsprox.protocol.v232.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.gjstr
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.outgoing.decoder.prot.GameServerProt

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
                    val length = buffer.gVarInt2()
                    val array =
                        IntArray(length) {
                            buffer.gVarInt2s()
                        }
                    arguments.addFirst(array)
                }
                'X' -> {
                    val length = buffer.gVarInt2()
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
}
