package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.varbit

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.varbit.VarbitLarge
import net.rsprox.protocol.session.Session

internal class VarbitLargeDecoder : ProxyMessageDecoder<VarbitLarge> {
    override val prot: ClientProt = GameServerProt.VARBIT_LARGE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarbitLarge {
        val hi = buffer.g1()
        val loRaw = buffer.g1()
        val lo = (loRaw - 128) and 0xFF
        val id = ((hi shl 8) or lo) and 0xFFFF
        val b1 = buffer.g1()
        val b0 = buffer.g1()
        val b3 = buffer.g1()
        val b2 = buffer.g1()
        val value = (b0 shl 24) or (b1 shl 16) or (b2 shl 8) or b3
        return VarbitLarge(
            id,
            value,
        )
    }
}
