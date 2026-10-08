package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.SpotanimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ProjectileOffset
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SpotanimSpecificV2Decoder : ProxyMessageDecoder<SpotanimSpecificV2> {
    override val prot: ClientProt = GameServerProt.SPOTANIM_SPECIFIC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SpotanimSpecificV2 {
        val packedDelay = buffer.g2Alt2()
        val id = buffer.g2Alt1().let { if (it == 65535) -1 else it }
        val offset = ProjectileOffset(buffer.g3())
        val height = buffer.g2sAlt2()
        val target = buffer.g4Alt1()
        val slot = buffer.g1Alt3()
        val rotationFlags = buffer.g1Alt1()
        return SpotanimSpecificV2(
            target,
            rotationFlags,
            offset,
            slot,
            height,
            id,
            packedDelay,
        )
    }
}
