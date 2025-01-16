package net.rsprox.protocol.v228.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAngle
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class IfSetAngleDecoder : ProxyMessageDecoder<IfSetAngle> {
    override val prot: ClientProt = GameServerProt.IF_SETANGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetAngle {
        val angleY = buffer.g2Alt2()
        val angleX = buffer.g2Alt3()
        val zoom = buffer.g2Alt3()
        val combinedId = buffer.gCombinedId()
        return IfSetAngle(
            combinedId.interfaceId,
            combinedId.componentId,
            angleX,
            angleY,
            zoom,
        )
    }
}
