package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class MinimapToggleDecoder : ProxyMessageDecoder<MinimapToggle> {
    override val prot: ClientProt = GameServerProt.MINIMAP_TOGGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MinimapToggle {
        val state = buffer.g1()
        return MinimapToggle(state)
    }
}
