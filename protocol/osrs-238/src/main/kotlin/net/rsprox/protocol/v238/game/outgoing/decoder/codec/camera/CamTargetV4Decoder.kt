package net.rsprox.protocol.v238.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV4
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v238.game.outgoing.decoder.prot.GameServerProt

internal class CamTargetV4Decoder : ProxyMessageDecoder<CamTargetV4> {
    override val prot: ClientProt = GameServerProt.CAM_TARGET_V4

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): CamTargetV4 {
        val type = buffer.g1()
        val value = buffer.g4Alt1()
        return CamTargetV4(
            when (type) {
                0 -> CamTargetV4.PlayerCamTarget(if (value == 65535) -1 else value)
                1 -> CamTargetV4.NpcCamTarget(if (value == 65535) -1 else value)
                2 -> CamTargetV4.WorldEntityTarget(if (value == 65535) -1 else value)
                3 -> CamTargetV4.CoordGridTarget(CoordGrid(value))
                else -> throw IllegalStateException("Unknown type: $type")
            },
        )
    }
}
