package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.SpotanimSpecific
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SpotanimSpecificDecoder : ProxyMessageDecoder<SpotanimSpecific> {
    override val prot: ClientProt = GameServerProt.SPOTANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SpotanimSpecific {
        val height = buffer.g2Alt3().toShort().toInt()
        val packedDelay = buffer.g2Alt1()
        val rotationFlags = buffer.g1Alt2()
        val target = buffer.g4()
        val id = buffer.g2Alt3().let { if (it == 65535) -1 else it }
        val slot = buffer.g1Alt1()
        return SpotanimSpecific(
            height,
            packedDelay,
            rotationFlags,
            target,
            id,
            slot,
        )
    }
}
