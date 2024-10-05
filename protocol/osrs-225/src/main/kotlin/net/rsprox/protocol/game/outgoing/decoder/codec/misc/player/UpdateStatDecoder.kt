package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStat
import net.rsprox.protocol.session.Session

public class UpdateStatDecoder : ProxyMessageDecoder<UpdateStat> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStat {
        val experience = buffer.g4Alt3()
        val stat = buffer.g1Alt3()
        val invisibleBoostedLevel = buffer.g1Alt2()
        val currentLevel = buffer.g1Alt1()
        return UpdateStat(
            stat,
            currentLevel,
            invisibleBoostedLevel,
            experience,
        )
    }
}
