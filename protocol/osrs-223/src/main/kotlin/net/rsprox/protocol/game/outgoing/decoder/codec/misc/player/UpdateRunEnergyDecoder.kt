package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunEnergy

@Consistent
public class UpdateRunEnergyDecoder : MessageDecoder<UpdateRunEnergy> {
    override val prot: ClientProt = GameServerProt.UPDATE_RUNENERGY

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateRunEnergy {
        val energy = buffer.g2()
        return UpdateRunEnergy(
            energy,
        )
    }
}
