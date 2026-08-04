package net.rsprox.protocol.v240.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtCycles
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v240.game.outgoing.decoder.prot.GameServerProt

internal class CamLookAtCyclesDecoder : ProxyMessageDecoder<CamLookAtCycles> {
    override val prot: ClientProt = GameServerProt.CAM_LOOKAT_CYCLES

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamLookAtCycles {
        val x = buffer.g2Alt2()
        val cycles = buffer.g2Alt2()
        val easing = buffer.g1Alt3()
        val z = buffer.g2()
        val heightRelative = buffer.g1Alt3() == 1
        val height = buffer.g2sAlt1()
        return CamLookAtCycles(
            x,
            z,
            height,
            cycles,
            easing,
            heightRelative,
        )
    }
}
