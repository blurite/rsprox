package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.session.Session

@Consistent
public class RunClientScriptDecoder : ProxyMessageDecoder<RunClientScript> {
    override val prot: ClientProt = GameServerProt.RUNCLIENTSCRIPT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RunClientScript {
        val types = buffer.gjstr()
        val arguments =
            buildList {
                for (char in types) {
                    if (char == 's') {
                        add(buffer.gjstr())
                    } else {
                        add(buffer.g4())
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
