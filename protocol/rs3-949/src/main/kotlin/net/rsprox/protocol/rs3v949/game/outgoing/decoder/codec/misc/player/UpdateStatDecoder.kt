package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateStat
import net.rsprox.protocol.session.Session

internal class UpdateStatDecoder : ProxyMessageDecoder<UpdateStat> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStat {
        val xp = buffer.g4Alt1()
        val level = buffer.g1Alt1()
        val skillId = buffer.g1Alt2()
        return UpdateStat(
            skillId,
            level,
            xp,
        )
    }
}
