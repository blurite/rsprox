package net.rsprox.protocol.v228.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt1
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAnim
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v228.game.outgoing.decoder.prot.GameServerProt

internal class IfSetAnimDecoder : ProxyMessageDecoder<IfSetAnim> {
    override val prot: ClientProt = GameServerProt.IF_SETANIM

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetAnim {
        val anim = buffer.g2Alt2()
        val combinedId = buffer.gCombinedIdAlt1()
        return IfSetAnim(
            combinedId.interfaceId,
            combinedId.componentId,
            anim,
        )
    }
}
