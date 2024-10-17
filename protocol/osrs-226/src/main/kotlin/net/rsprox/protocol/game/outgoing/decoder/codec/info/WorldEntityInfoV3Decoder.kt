package net.rsprox.protocol.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV3
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getWorld

public class WorldEntityInfoV3Decoder : ProxyMessageDecoder<WorldEntityInfoV3> {
    override val prot: ClientProt = GameServerProt.WORLDENTITY_INFO_V3

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): WorldEntityInfoV3 {
        val activeWorld = session.getActiveWorld()
        val world = session.getWorld(activeWorld)
        return world.worldEntityInfo.decode(
            buffer,
            CoordGrid(world.level, world.baseX, world.baseZ),
        )
    }
}
