package net.rsprox.protocol.v233.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetInteractionMode
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class ResetInteractionModeDecoder : ProxyMessageDecoder<ResetInteractionMode> {
    override val prot: ClientProt = GameServerProt.RESET_INTERACTION_MODE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ResetInteractionMode {
        val worldId = buffer.g2()
        return ResetInteractionMode(
            worldId,
        )
    }
}
