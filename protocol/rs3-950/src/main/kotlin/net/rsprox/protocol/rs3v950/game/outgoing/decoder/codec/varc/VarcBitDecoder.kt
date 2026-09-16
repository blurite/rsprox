package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varc

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varc.VarcBit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.gVarIntLE
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarcBitDecoder : ProxyMessageDecoder<VarcBit> {
    override val prot: ClientProt = GameServerProt.CLIENT_SETVARCBIT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarcBit {
        val id = buffer.gVarIntLE()
        val value = buffer.gVarIntLE()
        return VarcBit(
            id = id,
            value = value,
        )
    }
}
