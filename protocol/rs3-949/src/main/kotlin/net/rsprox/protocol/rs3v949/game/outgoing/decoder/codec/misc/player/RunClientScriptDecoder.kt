package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class RunClientScriptDecoder : ProxyMessageDecoder<RunClientScript> {
    override val prot: ClientProt = GameServerProt.RUN_CLIENT_SCRIPT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RunClientScript {
        val types = buffer.gjstr()
        val values = ArrayDeque<Any>(types.length)
        for (char in types.reversed()) {
            when (char) {
                'i' -> values.addFirst(buffer.g4())
                's' -> values.addFirst(buffer.gjstr())
                'l' -> values.addFirst(buffer.g8())
            }
        }
        val id = buffer.g4()
        return RunClientScript(
            id,
            types.toCharArray(),
            values,
        )
    }
}
