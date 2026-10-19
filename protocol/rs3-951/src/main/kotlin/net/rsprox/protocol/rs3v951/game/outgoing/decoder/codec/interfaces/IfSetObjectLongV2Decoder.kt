package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetObjectLongV2
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetObjectLongV2Decoder : ProxyMessageDecoder<IfSetObjectLongV2> {
    override val prot: ClientProt = GameServerProt.IF_SETOBJECT_LONG_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetObjectLongV2 {
        val quantityHigh = buffer.g4().toLong()
        val quantityLow = buffer.g4().toLong() and 0xFFFF_FFFFL
        val quantity = (quantityHigh shl 32) or quantityLow
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val rawObjId = buffer.g3Alt1()
        val objId = if (rawObjId == 0xFFFFFF) -1 else rawObjId
        return IfSetObjectLongV2(
            quantity,
            componentHash,
            objId,
        )
    }
}
