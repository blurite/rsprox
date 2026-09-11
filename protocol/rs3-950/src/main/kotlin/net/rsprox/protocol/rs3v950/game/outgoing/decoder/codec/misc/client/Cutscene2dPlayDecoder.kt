package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Cutscene2dPlay
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class Cutscene2dPlayDecoder : ProxyMessageDecoder<Cutscene2dPlay> {
    override val prot: ClientProt = GameServerProt.CUTSCENE2D_PLAY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Cutscene2dPlay {
        val id = buffer.g2()
        return Cutscene2dPlay(id)
    }
}
