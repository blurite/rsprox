package net.rsprox.protocol.v223.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelSelf
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

public class IfSetPlayerModelSelfDecoder : ProxyMessageDecoder<IfSetPlayerModelSelf> {
    override val prot: ClientProt = GameServerProt.IF_SETPLAYERMODEL_SELF

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetPlayerModelSelf {
        val copyObjs = buffer.g1Alt1() == 0
        val combinedId = buffer.gCombinedIdAlt1()
        return IfSetPlayerModelSelf(
            combinedId.interfaceId,
            combinedId.componentId,
            copyObjs,
        )
    }
}
