package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.npcinfo.initializeNpcInfo
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.playerinfo.initializePlayerInfo
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.info.playerinfo.readPlayerInfoInit
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class RebuildNormalDecoder : ProxyMessageDecoder<RebuildNormal> {
    override val prot: ClientProt = GameServerProt.REBUILD_NORMAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildNormal {
        val init = session.readPlayerInfoInit(buffer)
        val unused0 = buffer.g1()
        val format = buffer.g1Alt2()
        val npcCoordinateBits = buffer.g1Alt3()
        val baseChunkZ = buffer.g2Alt3()
        val baseChunkX = buffer.g2Alt1()
        val unused1 = buffer.g1()
        val message =
            RebuildNormal(
                baseChunkZ = baseChunkZ,
                format = format,
                npcCoordinateBits = npcCoordinateBits,
                unused = (unused0 shl 8) or unused1,
                baseChunkX = baseChunkX,
                worldAreaId = buffer.g2(),
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
