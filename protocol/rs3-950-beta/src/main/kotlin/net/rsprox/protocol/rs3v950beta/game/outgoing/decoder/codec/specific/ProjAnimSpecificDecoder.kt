package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecific
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ProjAnimSpecificDecoder : ProxyMessageDecoder<ProjAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecific {
        val progress = buffer.g2Alt3()
        val startHeight = buffer.g1Alt3().toByte().toInt()
        val startZ = buffer.g2Alt1()
        val level = buffer.g1Alt3()
        val source = buffer.g3()
        val angle = buffer.g1().let { if (it == 255) -1 else it }
        val flags = buffer.g1()
        val startX = buffer.g2Alt2()
        val deltaZ = buffer.g1Alt3().toByte().toInt()
        val endHeight = buffer.g1Alt2().toByte().toInt()
        val target = buffer.g3()
        val deltaX = buffer.g1Alt1().toByte().toInt()
        val endTime = buffer.g2()
        val startTime = buffer.g2Alt3()
        val id = buffer.g2()
        return ProjAnimSpecific(
            progress = progress,
            startHeight = startHeight,
            startZ = startZ,
            level = level,
            source = source,
            angle = angle,
            flags = flags,
            startX = startX,
            deltaZ = deltaZ,
            endHeight = endHeight,
            target = target,
            deltaX = deltaX,
            endTime = endTime,
            startTime = startTime,
            id = id,
        )
    }
}
