package net.rsprox.protocol.v224.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.PlayerSpotAnimSpecific
import net.rsprox.protocol.session.Session

public class PlayerSpotAnimSpecificDecoder : ProxyMessageDecoder<PlayerSpotAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PLAYER_SPOTANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerSpotAnimSpecific {
        val id = buffer.g2()
        val packed = buffer.g4()
        val slot = buffer.g1Alt1()
        val index = buffer.g2Alt2()
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
