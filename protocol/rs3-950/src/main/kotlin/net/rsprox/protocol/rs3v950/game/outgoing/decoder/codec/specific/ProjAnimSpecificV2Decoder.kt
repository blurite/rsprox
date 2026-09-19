package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ProjectileOffset
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ProjAnimSpecificV2Decoder : ProxyMessageDecoder<ProjAnimSpecificV2> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC_V2

    override fun decode(buffer: JagByteBuf, session: Session): ProjAnimSpecificV2 {
        val startX = buffer.g2()
        val source = buffer.g3Alt1()
        val endHeight = buffer.g2sAlt2()
        val startHeight = buffer.g2sAlt2()
        val deltaZ = buffer.g1Alt2().toByte().toInt()
        val angle = buffer.g1().let { if (it == 255) -1 else it }
        val flags = buffer.g1()
        val startZ = buffer.g2Alt3()
        val progress = buffer.g2Alt3()
        val endOffset = ProjectileOffset(buffer.g3Alt3())
        val id = buffer.g2Alt2()
        val startTime = buffer.g2Alt2()
        val startOffset = ProjectileOffset(buffer.g3Alt2())
        val endTime = buffer.g2Alt2()
        val level = buffer.g1Alt2()
        val target = buffer.g3()
        val deltaX = buffer.g1Alt2().toByte().toInt()
        return ProjAnimSpecificV2(
            startX,
            source,
            endHeight,
            startHeight,
            deltaZ,
            angle,
            flags,
            startZ,
            progress,
            endOffset,
            id,
            startTime,
            startOffset,
            endTime,
            level,
            target,
            deltaX,
        )
    }
}
