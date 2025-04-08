package net.rsprox.protocol.v230.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunWeight
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v230.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateRunWeightDecoder : ProxyMessageDecoder<UpdateRunWeight> {
    override val prot: ClientProt = GameServerProt.UPDATE_RUNWEIGHT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRunWeight {
        val runweight = buffer.g2()
        return UpdateRunWeight(
            runweight,
        )
    }
}
