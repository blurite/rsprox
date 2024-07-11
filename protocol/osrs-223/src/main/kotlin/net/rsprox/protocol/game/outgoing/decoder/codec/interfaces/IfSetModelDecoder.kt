package net.rsprox.protocol.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetModel
import net.rsprox.protocol.session.Session

public class IfSetModelDecoder : ProxyMessageDecoder<IfSetModel> {
    override val prot: ClientProt = GameServerProt.IF_SETMODEL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetModel {
        val combinedId = buffer.gCombinedIdAlt3()
        val model = buffer.g2Alt2()
        return IfSetModel(
            combinedId.interfaceId,
            combinedId.componentId,
            model,
        )
    }
}
