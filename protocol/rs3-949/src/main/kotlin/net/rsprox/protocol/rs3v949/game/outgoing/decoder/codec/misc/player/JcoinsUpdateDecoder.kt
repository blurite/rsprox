package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.JcoinsUpdate
import net.rsprox.protocol.session.Session

internal class JcoinsUpdateDecoder : ProxyMessageDecoder<JcoinsUpdate> {
    override val prot: ClientProt = GameServerProt.JCOINS_UPDATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): JcoinsUpdate {
        val jcoins = buffer.g4()
        return JcoinsUpdate(jcoins)
    }
}
