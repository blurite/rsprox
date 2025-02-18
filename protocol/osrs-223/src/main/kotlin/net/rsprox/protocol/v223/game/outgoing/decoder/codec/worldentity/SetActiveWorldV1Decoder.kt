package net.rsprox.protocol.v223.game.outgoing.decoder.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorldV1
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v223.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SetActiveWorldV1Decoder : ProxyMessageDecoder<SetActiveWorldV1> {
    override val prot: ClientProt = GameServerProt.SET_ACTIVE_WORLD_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetActiveWorldV1 {
        val type = buffer.g1()
        val index = buffer.g2()
        val activeLevel = buffer.g1()
        val world = session.getWorld(if (type == 0) -1 else index)
        world.level = activeLevel
        return if (type == 0) {
            SetActiveWorldV1(SetActiveWorldV1.RootWorldType(activeLevel))
        } else {
            SetActiveWorldV1(SetActiveWorldV1.DynamicWorldType(index, activeLevel))
        }
    }
}
