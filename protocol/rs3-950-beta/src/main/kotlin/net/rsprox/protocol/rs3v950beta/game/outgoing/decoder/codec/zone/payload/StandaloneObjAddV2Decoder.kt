package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.zone.payload

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class StandaloneObjAddV2Decoder : ProxyMessageDecoder<ObjAdd> {
    override val prot: ClientProt = GameServerProt.OBJ_ADD_V2
    private val payload = ObjAddV2Decoder()

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjAdd {
        val message = payload.decode(buffer, session)
        // The standalone descriptor includes one byte which the native callback never reads.
        // Enclosed-zone OBJ_ADD_V2 has no such byte.
        buffer.g1()
        return message
    }
}
