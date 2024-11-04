package net.rsprox.protocol.v223.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

internal class UpdateStatV1Decoder : ProxyMessageDecoder<UpdateStatV1> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStatV1 {
        val experience = buffer.g4()
        val currentLevel = buffer.g1Alt3()
        val stat = buffer.g1Alt3()
        return UpdateStatV1(
            stat,
            currentLevel,
            experience,
        )
    }
}
