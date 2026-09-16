package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.rs3v949.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.session.Session

internal class RebuildNormalDecoder : ProxyMessageDecoder<RebuildNormal> {
    override val prot: ClientProt = GameServerProt.REBUILD_NORMAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): RebuildNormal {
        val ownIndex = session.localPlayerIndex.takeIf { it != -1 }

        var initBlock: PlayerInfoInitBlock? = null
        if (buffer.readableBytes() >= GPI_TABLE_BYTES) {
            var selfX = 0
            var selfY = 0
            var selfPlane = 0
            val positions = IntArray(MAX_SLOT + 1)
            buffer.buffer.toBitBuf().use { bits ->
                val selfTileId = bits.gBits(30)
                selfX = selfTileId and 0x3FFF
                selfY = (selfTileId ushr 14) and 0x3FFF
                selfPlane = (selfTileId ushr 28) and 0x3
                for (slot in 1..MAX_SLOT) {
                    if (slot == ownIndex) continue
                    positions[slot] = bits.gBits(20)
                }
            }
            initBlock =
                PlayerInfoInitBlock(
                    localPlayerIndex = ownIndex ?: -1,
                    localPlayerLevel = selfPlane,
                    localPlayerX = selfX,
                    localPlayerZ = selfY,
                    positions = positions,
                )
        }

        if (buffer.readableBytes() < TRAILER_HEADER_BYTES) {
            return RebuildNormal(
                playerInfoInitBlock = initBlock,
                playerCoordX = 0,
                playerCoordY = 0,
                coordBitWidth = 0,
                trailerAligned = false,
                worldAreaTypeId = -1,
                worldSouthWest = 0,
                worldNorthEast = 0,
                baseTileX = 0,
                baseTileZ = 0,
            )
        }

        val playerCoordX = buffer.g2()
        val playerCoordY = buffer.g2Alt2()
        buffer.g2()
        val coordBitWidth = buffer.g1()
        val magicFive = buffer.g1()

        val baseTileX = (playerCoordX - IVAR10_ZONE_OFFSET) * 8
        val baseTileZ = (playerCoordY - IVAR10_ZONE_OFFSET) * 8

        if (magicFive != 5 || buffer.readableBytes() < TRAILER_TAIL_BYTES) {
            return RebuildNormal(
                initBlock, playerCoordX, playerCoordY, coordBitWidth, trailerAligned = false,
                worldAreaTypeId = -1, worldSouthWest = 0, worldNorthEast = 0,
                baseTileX = baseTileX, baseTileZ = baseTileZ,
            )
        }

        val worldAreaTypeId = buffer.g2()
        val worldSouthWest = buffer.g4()
        val worldNorthEast = buffer.g4()
        return RebuildNormal(
            initBlock, playerCoordX, playerCoordY, coordBitWidth, trailerAligned = true,
            worldAreaTypeId, worldSouthWest, worldNorthEast, baseTileX, baseTileZ,
        )
    }

    private companion object {
        private const val MAX_SLOT = 0x7FF
        private const val IVAR10_ZONE_OFFSET = 16
        private const val GPI_TABLE_BITS = 30 + (MAX_SLOT - 1) * 20
        private const val GPI_TABLE_BYTES = (GPI_TABLE_BITS + 7) / 8
        private const val TRAILER_HEADER_BYTES = 8
        private const val TRAILER_TAIL_BYTES = 10
    }
}
