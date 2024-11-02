package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV2
import net.rsprox.protocol.session.Session

public class UpdateStatV2Decoder : ProxyMessageDecoder<UpdateStatV2> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateStatV2 {
        val stat = buffer.g1Alt3()
        val invisibleBoostedLevel = buffer.g1()
        val experience = buffer.g4Alt2()
        val currentLevel = buffer.g1Alt3()
        return UpdateStatV2(
            stat,
            currentLevel,
            invisibleBoostedLevel,
            experience,
        )
    }
}
