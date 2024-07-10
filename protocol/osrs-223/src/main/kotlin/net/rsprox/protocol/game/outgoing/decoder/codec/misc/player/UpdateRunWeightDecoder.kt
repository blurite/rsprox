package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunWeight

@Consistent
public class UpdateRunWeightDecoder : MessageDecoder<UpdateRunWeight> {
    override val prot: ClientProt = GameServerProt.UPDATE_RUNWEIGHT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateRunWeight {
        val runweight = buffer.g2()
        return UpdateRunWeight(
            runweight,
        )
    }
}
