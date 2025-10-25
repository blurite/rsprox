package net.rsprox.protocol.v224.game.outgoing.decoder.codec.map

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.bitbuffer.toBitBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.xtea.XteaKey
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.util.PlayerInfoInitBlock
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.GenericWorldEntityInfoClient
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.protocol.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.game.outgoing.model.map.StaticRebuildMessage
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.allocateWorld
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v224.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.v224.game.outgoing.model.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.v224.game.outgoing.model.info.playerinfo.PlayerInfoClient

internal class StaticRebuildDecoder(
    private val huffmanCodec: HuffmanCodec,
    private val cache: CacheProvider,
) : ProxyMessageDecoder<StaticRebuildMessage> {
    override val prot: ClientProt = GameServerProt.REBUILD_NORMAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): StaticRebuildMessage {
        val playerInfoInitBlock =
            if (buffer.isReadable(MINIMUM_REBUILD_LOGIN_CAPACITY)) {
                buffer.buffer
                    .toBitBuf()
                    .use { bitBuf ->
                        val localPlayerAbsolutePosition = bitBuf.gBits(30)
                        val lowResolutionPositions = IntArray(2048)
                        for (i in 1..<lowResolutionPositions.size) {
                            if (i == session.localPlayerIndex) continue
                            lowResolutionPositions[i] = bitBuf.gBits(18)
                        }
                        PlayerInfoInitBlock(
                            session.localPlayerIndex,
                            CoordGrid(localPlayerAbsolutePosition),
                            lowResolutionPositions,
                        )
                    }
            } else {
                null
            }
        val zoneZ = buffer.g2Alt1()
        val worldArea = buffer.g2sAlt3()
        val zoneX = buffer.g2Alt2()
        val xteaCount = buffer.g2()
        val keys =
            buildList {
                for (i in 0..<xteaCount) {
                    add(
                        XteaKey(
                            buffer.g4(),
                            buffer.g4(),
                            buffer.g4(),
                            buffer.g4(),
                        ),
                    )
                }
            }
        return if (playerInfoInitBlock != null) {
            val message =
                RebuildLogin(
                    zoneX,
                    zoneZ,
                    worldArea,
                    keys,
                    playerInfoInitBlock,
                )
            val world =
                session.allocateWorld(
                    -1,
                    PlayerInfoClient(
                        session.localPlayerIndex,
                        huffmanCodec,
                    ),
                    NpcInfoClient(cache),
                    GenericWorldEntityInfoClient(),
                )
            world.baseX = (zoneX - 6) * 8
            world.baseZ = (zoneZ - 6) * 8
            world.playerInfo.gpiInit(playerInfoInitBlock)
            message
        } else {
            val world = session.getWorld(-1)
            world.baseX = (zoneX - 6) * 8
            world.baseZ = (zoneZ - 6) * 8
            RebuildNormal(
                zoneX,
                zoneZ,
                worldArea,
                keys,
            )
        }
    }

    private companion object {
        /**
         * Maximum num of bits that can be carried without using up an entire byte.
         * We need this constant to ensure that our calculations "round up" without ever creating
         * a new byte that shouldn't have been there to begin with.
         */
        private const val MAX_BITS_BELOW_BYTE: Int = Byte.SIZE_BITS - 1

        /**
         * The minimum number of mapsquares that could ever be sent via static rebuilds.
         * The number can always be higher, but never below 4 with the current 104x104
         * size buildarea.
         */
        private const val MINIMUM_NUM_OF_MAPSQUARES: Int = 4

        /**
         * Minimum number of bytes necessary to transmit the player info initialization block.
         */
        private const val MINIMUM_PLAYER_INFO_INIT_SIZE: Int = (18 * 2046 + 30 + MAX_BITS_BELOW_BYTE) ushr 3

        /**
         * The minimum number of bytes necessary to transmit everything but xteas for rebuild normal.
         */
        private const val MINIMUM_REBUILD_HEADER_BYTES: Int =
            Short.SIZE_BYTES +
                Short.SIZE_BYTES +
                Short.SIZE_BYTES

        /**
         * The minimum number of bytes necessary to transmit the smallest possible xtea block.
         */
        private const val MINIMUM_REBUILD_XTEA_BYTES: Int =
            Short.SIZE_BYTES +
                MINIMUM_NUM_OF_MAPSQUARES * (4 * Int.SIZE_BYTES)

        /**
         * Minimum number of bytes necessary for rebuild login to work.
         * It should be noted that this assumes that the xtea block requires at least 4 mapsquares,
         * which would be the bare minimum at specific coordinates. The number could always be higher.
         */
        private const val MINIMUM_REBUILD_LOGIN_CAPACITY: Int =
            MINIMUM_PLAYER_INFO_INIT_SIZE +
                MINIMUM_REBUILD_HEADER_BYTES +
                MINIMUM_REBUILD_XTEA_BYTES
    }
}
