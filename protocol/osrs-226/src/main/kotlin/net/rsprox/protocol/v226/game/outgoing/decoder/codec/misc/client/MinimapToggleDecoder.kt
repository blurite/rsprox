package net.rsprox.protocol.v226.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

@Consistent
public class MinimapToggleDecoder : ProxyMessageDecoder<MinimapToggle> {
    override val prot: ClientProt = GameServerProt.MINIMAP_TOGGLE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MinimapToggle {
        val state = buffer.g1()
        return MinimapToggle(state)
    }
}
