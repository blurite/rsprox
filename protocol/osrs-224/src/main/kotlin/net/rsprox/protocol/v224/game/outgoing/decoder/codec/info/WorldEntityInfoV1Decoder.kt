package net.rsprox.protocol.v224.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getWorld

public class WorldEntityInfoV1Decoder : ProxyMessageDecoder<WorldEntityInfo> {
    override val prot: ClientProt = GameServerProt.WORLDENTITY_INFO_V1

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): WorldEntityInfo {
        val activeWorld = session.getActiveWorld()
        val world = session.getWorld(activeWorld)
        return world.worldEntityInfo.decode(
            buffer,
            CoordGrid(world.level, world.baseX, world.baseZ),
            1,
        )
    }
}
