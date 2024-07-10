package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript

@Consistent
public class RunClientScriptDecoder : MessageDecoder<RunClientScript> {
    override val prot: ClientProt = GameServerProt.RUNCLIENTSCRIPT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
