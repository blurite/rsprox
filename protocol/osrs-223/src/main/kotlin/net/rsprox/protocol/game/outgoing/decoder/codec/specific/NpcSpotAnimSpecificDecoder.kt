package net.rsprox.protocol.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.specific.NpcSpotAnimSpecific
import net.rsprox.protocol.session.Session

public class NpcSpotAnimSpecificDecoder : ProxyMessageDecoder<NpcSpotAnimSpecific> {
    override val prot: ClientProt = GameServerProt.NPC_SPOTANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcSpotAnimSpecific {
        val id = buffer.g2Alt2()
        val index = buffer.g2Alt1()
        val slot = buffer.g1Alt3()
        val packed = buffer.g4()
        val height = packed ushr 16
        val delay = packed and 0xFFFF
        return NpcSpotAnimSpecific(
            index,
            id,
            slot,
            height,
            delay,
        )
    }
}
