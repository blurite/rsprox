package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo.initializeNpcInfo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.initializePlayerInfo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.readPlayerInfoInit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class RebuildNormalDecoder : ProxyMessageDecoder<RebuildNormal> {
    override val prot: ClientProt = GameServerProt.REBUILD_NORMAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildNormal {
        val init = session.readPlayerInfoInit(buffer)
        val message =
            RebuildNormal(
                baseChunkZ = buffer.g2Alt2(),
                format = buffer.g1(),
                npcCoordinateBits = buffer.g1Alt1(),
                reserved = buffer.g2(),
                baseChunkX = buffer.g2(),
                templateId = buffer.g2(),
                minimumCoordinate = buffer.g4(),
                maximumCoordinate = buffer.g4(),
                sceneSize = SCENE_SIZE,
                playerInfoInit = init,
            )
        require(message.format == 5) { "Unsupported REBUILD_NORMAL format ${message.format}" }
        require(buffer.readableBytes() == 0) { "Trailing REBUILD_NORMAL bytes" }
        session.initializeNpcInfo(message.npcCoordinateBits, init != null)
        session.initializePlayerInfo(init)
        return message
    }
}
