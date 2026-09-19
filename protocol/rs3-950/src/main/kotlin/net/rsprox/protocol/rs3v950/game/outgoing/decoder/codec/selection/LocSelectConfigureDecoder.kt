package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectConfigure
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocSelectConfigureDecoder : ProxyMessageDecoder<LocSelectConfigure> {
    override val prot: ClientProt = GameServerProt.LOCSELECT_CONFIGURE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocSelectConfigure {
        val id = buffer.g4Alt1()
        val enabled = buffer.g1Alt1() == 1
        val from = buffer.g4Alt1()
        val count = buffer.g1Alt2()
        val to = buffer.g4()
        return LocSelectConfigure(
            id,
            enabled,
            from,
            count,
            to,
        )
    }
}
