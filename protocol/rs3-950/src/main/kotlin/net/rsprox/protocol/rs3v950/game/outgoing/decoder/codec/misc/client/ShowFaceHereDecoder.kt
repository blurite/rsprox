package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ShowFaceHere
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ShowFaceHereDecoder : ProxyMessageDecoder<ShowFaceHere> {
    override val prot: ClientProt = GameServerProt.SHOW_FACE_HERE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ShowFaceHere {
        val enabled = buffer.g1() == 1
        return ShowFaceHere(
            enabled,
        )
    }
}
