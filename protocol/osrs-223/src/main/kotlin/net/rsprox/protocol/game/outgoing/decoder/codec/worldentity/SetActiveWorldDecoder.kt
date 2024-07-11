package net.rsprox.protocol.game.outgoing.decoder.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorld
import net.rsprox.protocol.session.Session

@Consistent
public class SetActiveWorldDecoder : ProxyMessageDecoder<SetActiveWorld> {
    override val prot: ClientProt = GameServerProt.SET_ACTIVE_WORLD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetActiveWorld {
        val type = buffer.g1()
        val index = buffer.g2()
        val activeLevel = buffer.g1()
        return if (type == 0) {
            SetActiveWorld(SetActiveWorld.RootWorldType(activeLevel))
        } else {
            SetActiveWorld(SetActiveWorld.DynamicWorldType(index, activeLevel))
        }
    }
}
