package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ProjectileOffset
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ProjAnimSpecificV2Decoder : ProxyMessageDecoder<ProjAnimSpecificV2> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecificV2 {
        val startHeight = buffer.g2sAlt1()
        val id = buffer.g2()
        val level = buffer.g1()
        val startZ = buffer.g2()
        val angle = buffer.g1Alt1().let { if (it == 255) -1 else it }
        val startX = buffer.g2()
        val endOffset = buffer.g3Alt2()
        val startTime = buffer.g2Alt1()
        val deltaX = buffer.g1Alt1().toByte().toInt()
        val startOffset = buffer.g3Alt2()
        val source = buffer.g3Alt1()
        val flags = buffer.g1Alt2()
        val deltaZ = buffer.g1Alt2().toByte().toInt()
        val endHeight = buffer.g2sAlt1()
        val endTime = buffer.g2Alt3()
        val target = buffer.g3Alt3()
        val progress = buffer.g2Alt3()
        return ProjAnimSpecificV2(
            endTime = endTime,
            deltaZ = deltaZ,
            startX = startX,
            endOffset = ProjectileOffset(endOffset),
            angle = angle,
            startHeight = startHeight,
            flags = flags,
            target = target,
            startZ = startZ,
            deltaX = deltaX,
            id = id,
            source = source,
            level = level,
            startTime = startTime,
            endHeight = endHeight,
            progress = progress,
            startOffset = ProjectileOffset(startOffset),
        )
    }
}
