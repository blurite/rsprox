package net.rsprox.protocol.v229.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetRotateSpeed
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

internal class IfSetRotateSpeedDecoder : ProxyMessageDecoder<IfSetRotateSpeed> {
    override val prot: ClientProt = GameServerProt.IF_SETROTATESPEED

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetRotateSpeed {
        val ySpeed = buffer.g2Alt3()
        val combinedId = buffer.gCombinedIdAlt1()
        val xSpeed = buffer.g2Alt2()
        return IfSetRotateSpeed(
            combinedId.interfaceId,
            combinedId.componentId,
            xSpeed,
            ySpeed,
        )
    }
}
