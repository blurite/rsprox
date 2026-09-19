package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.events

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.events.TransmitVarVerifyId
import net.rsprox.protocol.session.Session

internal class TransmitVarVerifyIdDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<TransmitVarVerifyId> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TransmitVarVerifyId {
        val verifyId = buffer.g4()
        return TransmitVarVerifyId(
            verifyId,
        )
    }
}
