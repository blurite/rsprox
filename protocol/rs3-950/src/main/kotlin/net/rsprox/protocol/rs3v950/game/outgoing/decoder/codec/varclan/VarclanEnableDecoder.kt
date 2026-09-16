package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.varclan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.VarclanEnable
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class VarclanEnableDecoder : ProxyMessageDecoder<VarclanEnable> {
    override val prot: ClientProt = GameServerProt.VARCLAN_ENABLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): VarclanEnable = VarclanEnable
}
