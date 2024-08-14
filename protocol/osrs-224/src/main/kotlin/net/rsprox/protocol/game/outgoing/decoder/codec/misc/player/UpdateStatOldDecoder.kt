package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatOld
import net.rsprox.protocol.session.Session

public class UpdateStatOldDecoder : ProxyMessageDecoder<UpdateStatOld> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT_OLD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStatOld {
        val experience = buffer.g4()
        val currentLevel = buffer.g1Alt3()
        val stat = buffer.g1Alt3()
        return UpdateStatOld(
            stat,
            currentLevel,
            experience,
        )
    }
}
