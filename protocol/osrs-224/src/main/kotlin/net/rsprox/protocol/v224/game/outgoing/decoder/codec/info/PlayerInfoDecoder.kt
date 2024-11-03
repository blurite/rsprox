package net.rsprox.protocol.v224.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt

internal class PlayerInfoDecoder : ProxyMessageDecoder<PlayerInfo> {
    override val prot: ClientProt = GameServerProt.PLAYER_INFO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerInfo {
        val activeWorld = session.getActiveWorld()
        val world = session.getWorld(activeWorld)
        return world.playerInfo.decode(buffer.buffer)
    }
}
