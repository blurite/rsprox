package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetObjectLongV2
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetObjectLongV2Decoder : ProxyMessageDecoder<IfSetObjectLongV2> {
    override val prot: ClientProt = GameServerProt.IF_SETOBJECT_LONG_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetObjectLongV2 {
        val quantityHigh = buffer.g4Alt2().toLong()
        val quantityLow = buffer.g4Alt2().toLong() and 0xFFFF_FFFFL
        val quantity = (quantityHigh shl 32) or quantityLow
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFF_FFFFL
        val objId = buffer.g3().let { if (it == 0xFFFFFF) -1 else it }
        return IfSetObjectLongV2(
            quantity,
            componentHash,
            objId,
        )
    }
}
