package net.rsprox.protocol.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getNpcInfoBaseCoord
import net.rsprox.protocol.session.getWorld

public class NpcInfoSmallDecoder : ProxyMessageDecoder<NpcInfo> {
    override val prot: ClientProt = GameServerProt.NPC_INFO_SMALL_V4

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): NpcInfo {
        val activeWorld = session.getActiveWorld()
        val world = session.getWorld(activeWorld)
        return world.npcInfo.decode(
            buffer.buffer,
            false,
            session.getNpcInfoBaseCoord(),
        )
    }
}
