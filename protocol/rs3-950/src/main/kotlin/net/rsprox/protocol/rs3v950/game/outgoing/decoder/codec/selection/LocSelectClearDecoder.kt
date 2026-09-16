package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.selection

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectClear
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class LocSelectClearDecoder : ProxyMessageDecoder<LocSelectClear> {
    override val prot: ClientProt = GameServerProt.LOCSELECT_CLEAR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LocSelectClear = LocSelectClear
}
