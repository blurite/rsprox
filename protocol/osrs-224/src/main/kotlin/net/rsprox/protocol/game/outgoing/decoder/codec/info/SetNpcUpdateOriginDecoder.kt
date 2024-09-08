package net.rsprox.protocol.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.SetNpcUpdateOrigin
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.session.setNpcInfoBaseCoord

@Consistent
public class SetNpcUpdateOriginDecoder : ProxyMessageDecoder<SetNpcUpdateOrigin> {
    override val prot: ClientProt = GameServerProt.SET_NPC_UPDATE_ORIGIN

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetNpcUpdateOrigin {
        val originX = buffer.g1()
        val originZ = buffer.g1()
        val world = session.getWorld(session.getActiveWorld())
        val coord =
            CoordGrid(
                world.level,
                world.baseX + originX,
                world.baseZ + originZ,
            )
        session.setNpcInfoBaseCoord(coord)
        return SetNpcUpdateOrigin(
            originX,
            originZ,
        )
    }
}
