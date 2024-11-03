package net.rsprox.protocol.v225.game.outgoing.decoder.codec.varp

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.varp.VarpReset
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class VarpResetDecoder : ProxyMessageDecoder<VarpReset> {
    override val prot: ClientProt = GameServerProt.VARP_RESET

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarpReset {
        return VarpReset
    }
}
