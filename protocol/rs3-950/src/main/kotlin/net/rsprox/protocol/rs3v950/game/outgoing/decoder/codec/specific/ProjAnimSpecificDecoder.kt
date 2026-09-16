package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.specific

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecific
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ProjAnimSpecificDecoder : ProxyMessageDecoder<ProjAnimSpecific> {
    override val prot: ClientProt = GameServerProt.PROJANIM_SPECIFIC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ProjAnimSpecific {
        val target = buffer.g3Alt2()
        val endHeight = buffer.g1Alt2().toByte().toInt()
        val startZ = buffer.g2Alt1()
        val deltaZ = buffer.g1Alt2().toByte().toInt()
        val startTime = buffer.g2()
        val startHeight = buffer.g1Alt2().toByte().toInt()
        // Only the low flag bits have native consumer evidence; retain the transmitted byte.
        val flags = buffer.g1()
        val distance = buffer.g2Alt1()
        val startX = buffer.g2Alt1()
        val endTime = buffer.g2Alt3()
        val slope = buffer.g1()
        val source = buffer.g3()
        val deltaX = buffer.g1Alt1().toByte().toInt()
        val id = buffer.g2()
        val level = buffer.g1()
        return ProjAnimSpecific(
            target, endHeight, startZ, deltaZ, startTime, startHeight, flags, distance,
            startX, endTime, slope, source, deltaX, id, level,
        )
    }
}
