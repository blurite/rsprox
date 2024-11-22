package net.rsprox.protocol.v227.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

internal class UpdateStatV2Decoder : ProxyMessageDecoder<UpdateStatV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStatV2 {
        val experience = buffer.g4()
        val invisibleBoostedLevel = buffer.g1()
        val currentLevel = buffer.g1()
        val stat = buffer.g1()
        return UpdateStatV2(
            stat,
            currentLevel,
            invisibleBoostedLevel,
            experience,
        )
    }
}
