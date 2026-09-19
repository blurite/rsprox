package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocAnimSpecificDecoder : ProxyMessageDecoder<LocAnimSpecific> {
    override val prot: ClientProt = GameServerProt.LOC_ANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocAnimSpecific {
        val shapeRotation = buffer.g1()
        // This fixed-size packet only carries the base location-shape encoding.
        require(shapeRotation and 0x80 == 0) { "Extended location shape in fixed-size LOC_ANIM_SPECIFIC" }
        val delay = buffer.g1()
        val animation = buffer.g4Alt2()
        val coordinate = buffer.g4Alt3()
        return LocAnimSpecific(
            shapeRotation,
            delay,
            animation,
            coordinate,
        )
    }
}
