package net.rsprox.protocol.v226.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getNpcInfoBaseCoord
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

public class NpcInfoSmallV5Decoder : ProxyMessageDecoder<NpcInfo> {
    override val prot: ClientProt = GameServerProt.NPC_INFO_SMALL_V5

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
