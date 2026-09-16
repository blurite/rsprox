package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ResetAnimsDecoder : ProxyMessageDecoder<ResetAnims> {
    override val prot: ClientProt = GameServerProt.RESET_ANIMS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResetAnims = ResetAnims
}
