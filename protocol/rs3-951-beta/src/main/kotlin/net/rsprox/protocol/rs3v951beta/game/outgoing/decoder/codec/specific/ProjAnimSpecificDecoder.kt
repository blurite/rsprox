package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecific
import net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ProjAnimSpecificDecoder : ProxyMessageDecoder<ProjAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecific {
        val endTime = buffer.g2Alt3()
        val target = buffer.g3()
        val startTime = buffer.g2Alt3()
        val startZ = buffer.g2Alt3()
        val source = buffer.g3Alt1()
        val startHeight = buffer.g1().toByte().toInt()
        val id = buffer.g2Alt2()
        val angle = buffer.g1Alt2().let { if (it == 255) -1 else it }
        val startX = buffer.g2Alt3()
        val flags = buffer.g1()
        val deltaX = buffer.g1().toByte().toInt()
        val deltaZ = buffer.g1Alt1().toByte().toInt()
        val level = buffer.g1()
        val progress = buffer.g2()
        val endHeight = buffer.g1Alt3().toByte().toInt()
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
