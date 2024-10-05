package net.rsprox.protocol.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.getPlayerInfoClient

public class PlayerInfoDecoder : ProxyMessageDecoder<PlayerInfo> {
    override val prot: ClientProt = GameServerProt.PLAYER_INFO

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PlayerInfo {
        val playerInfo = session.getPlayerInfoClient()
        return playerInfo.decode(buffer.buffer)
    }
}
