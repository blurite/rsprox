package net.rsprox.protocol.v226.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.PlayerSpotAnimSpecific
import net.rsprox.protocol.session.Session

public class PlayerSpotAnimSpecificDecoder : ProxyMessageDecoder<PlayerSpotAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PLAYER_SPOTANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerSpotAnimSpecific {
        val packed = buffer.g4Alt2()
        val id = buffer.g2Alt3()
        val slot = buffer.g1()
        val index = buffer.g2Alt1()
        val height = packed ushr 16
        val delay = packed and 0xFFFF
        return PlayerSpotAnimSpecific(
            index,
            id,
            slot,
            height,
            delay,
        )
    }
}
