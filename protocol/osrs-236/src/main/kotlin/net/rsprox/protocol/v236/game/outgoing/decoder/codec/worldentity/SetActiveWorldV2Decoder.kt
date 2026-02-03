package net.rsprox.protocol.v236.game.outgoing.decoder.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorldV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.session.setActiveWorld
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SetActiveWorldV2Decoder : ProxyMessageDecoder<SetActiveWorldV2> {
    override val prot: ClientProt = GameServerProt.SET_ACTIVE_WORLD_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetActiveWorldV2 {
        val index = buffer.g2s()
        val activeLevel = buffer.g1()
        session.setActiveWorld(index, activeLevel)
        val world = session.getWorld(index)
        world.level = activeLevel
        return if (index == -1) {
            SetActiveWorldV2(SetActiveWorldV2.RootWorldType(activeLevel))
        } else {
            SetActiveWorldV2(SetActiveWorldV2.DynamicWorldType(index, activeLevel))
        }
    }
}
