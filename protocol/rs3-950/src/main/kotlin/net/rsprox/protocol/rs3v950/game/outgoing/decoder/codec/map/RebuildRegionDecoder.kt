package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.npcinfo.initializeNpcInfo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.initializePlayerInfo
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.info.playerinfo.readPlayerInfoInit
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class RebuildRegionDecoder : ProxyMessageDecoder<RebuildRegion> {
    override val prot: ClientProt = GameServerProt.REBUILD_REGION

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildRegion {
        val init = session.readPlayerInfoInit(buffer)
        val baseChunkZ = buffer.g2Alt1()
        val unused = buffer.g1()
        val npcSceneValue = buffer.g1()
        val format = buffer.g1()
        val mode = buffer.g1Alt1()
        val baseChunkX = buffer.g2Alt1()
        require(format == 5 && mode in 1..4) { "Unsupported region format/mode $format/$mode" }
        val regionOriginX = buffer.g2()
        val regionOriginZ = buffer.g2()
        val rows = buffer.g1()
        val columns = buffer.g1()
        require(4 * rows * columns <= buffer.readableBytes() * 8) { "Truncated region presence bits" }
        val templates =
            buffer.buffer.toBitBuf().use { bits ->
                List(4) {
                    List(rows) {
                        List(columns) {
                            require(bits.isReadable(1)) { "Missing region presence bit" }
                            if (bits.gBits(1) == 0) {
                                -1
                            } else {
                                require(bits.isReadable(26)) { "Truncated region template" }
                                bits.gBits(26)
                            }
                        }
                    }
                }
            }
        require(buffer.readableBytes() == 0) { "Trailing REBUILD_REGION bytes" }
        session.initializeNpcInfo(npcSceneValue, init != null)
        session.initializePlayerInfo(init)
        return RebuildRegion(
            baseChunkZ,
            unused,
            npcSceneValue,
            format,
            mode,
            baseChunkX,
            regionOriginX,
            regionOriginZ,
            rows,
            columns,
            templates,
            SCENE_SIZE,
            init,
        )
    }
}
