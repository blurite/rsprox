package net.rsprox.protocol.v235.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBaseColour
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v235.game.outgoing.decoder.prot.GameServerProt

internal class IfSetPlayerModelBaseColourDecoder : ProxyMessageDecoder<IfSetPlayerModelBaseColour> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_BASECOLOUR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelBaseColour {
        val index = buffer.g1()
        val colour = buffer.g1Alt3()
        val combinedId = buffer.gCombinedId()
        return IfSetPlayerModelBaseColour(
            combinedId.interfaceId,
            combinedId.componentId,
            index,
            colour,
        )
    }
}
