package net.rsprox.protocol.v229.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v229.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ResetAnimsDecoder : ProxyMessageDecoder<ResetAnims> {
    override val prot: ClientProt = GameServerProt.RESET_ANIMS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResetAnims {
        return ResetAnims
    }
}
