package net.rsprox.protocol.v234.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt2
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetRotateSpeed
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v234.game.outgoing.decoder.prot.GameServerProt

internal class IfSetRotateSpeedDecoder : ProxyMessageDecoder<IfSetRotateSpeed> {
    override val prot: ClientProt = GameServerProt.IF_SETROTATESPEED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetRotateSpeed {
        val combinedId = buffer.gCombinedIdAlt2()
        val ySpeed = buffer.g2Alt1()
        val xSpeed = buffer.g2Alt1()
        return IfSetRotateSpeed(
            combinedId.interfaceId,
            combinedId.componentId,
            xSpeed,
            ySpeed,
        )
    }
}
