package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Js5Reload
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class Js5ReloadDecoder : ProxyMessageDecoder<Js5Reload> {
    override val prot: ClientProt = GameServerProt.JS5_RELOAD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Js5Reload = Js5Reload
}
