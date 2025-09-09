package net.rsprox.protocol.v233.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.SetInteractionMode
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SetInteractionModeDecoder : ProxyMessageDecoder<SetInteractionMode> {
    override val prot: ClientProt = GameServerProt.SET_INTERACTION_MODE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetInteractionMode {
        val worldId = buffer.g2()
        val tileInteractionMode = buffer.g1()
        val entityInteractionMode = buffer.g1()
        return SetInteractionMode(
            worldId,
            tileInteractionMode,
            entityInteractionMode,
        )
    }
}
