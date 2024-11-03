package net.rsprox.protocol.v224.game.outgoing.decoder.codec.worldentity

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorld
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SetActiveWorldDecoder : ProxyMessageDecoder<SetActiveWorld> {
    override val prot: ClientProt = GameServerProt.SET_ACTIVE_WORLD

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetActiveWorld {
        val type = buffer.g1()
        val index = buffer.g2()
        val activeLevel = buffer.g1()
        val world = session.getWorld(if (type == 0) -1 else index)
        world.level = activeLevel
        return if (type == 0) {
            SetActiveWorld(SetActiveWorld.RootWorldType(activeLevel))
        } else {
            SetActiveWorld(SetActiveWorld.DynamicWorldType(index, activeLevel))
        }
    }
}
