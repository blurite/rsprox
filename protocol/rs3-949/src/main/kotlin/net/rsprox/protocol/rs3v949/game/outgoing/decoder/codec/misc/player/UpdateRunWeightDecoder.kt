package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.UpdateRunWeight
import net.rsprox.protocol.session.Session

internal class UpdateRunWeightDecoder : ProxyMessageDecoder<UpdateRunWeight> {
    override val prot: ClientProt = GameServerProt.UPDATE_RUN_WEIGHT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateRunWeight {
        val weight = buffer.g2()
        return UpdateRunWeight(weight)
    }
}
