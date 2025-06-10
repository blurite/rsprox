package net.rsprox.protocol.v231.game.outgoing.decoder.codec.info

import net.rsprot.buffer.JagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.protocol.ClientProt
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityUpdateType
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.allocateWorld
import net.rsprox.protocol.session.getActiveWorld
import net.rsprox.protocol.session.getWorld
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.v231.game.outgoing.model.info.npcinfo.NpcInfoClient
import net.rsprox.protocol.v231.game.outgoing.model.info.playerinfo.PlayerInfoClient

internal class WorldEntityInfoV5Decoder(
    private val huffmanCodec: HuffmanCodec,
    private val cache: CacheProvider,
) : ProxyMessageDecoder<WorldEntityInfo> {
    override val prot: ClientProt = GameServerProt.WORLDENTITY_INFO_V5

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): WorldEntityInfo {
        val activeWorld = session.getActiveWorld()
        val world = session.getWorld(activeWorld)
        val message =
            world.worldEntityInfo.decode(
                buffer,
                CoordGrid(world.level, world.baseX, world.baseZ),
                5,
            )
        for ((index, update) in message.updates) {
            when (update) {
                is WorldEntityUpdateType.ActiveV2 -> {
                }
                WorldEntityUpdateType.HighResolutionToLowResolution -> {
                }
                is WorldEntityUpdateType.LowResolutionToHighResolutionV2 -> {
                    session.allocateWorld(
                        index,
                        PlayerInfoClient(
                            session.localPlayerIndex,
                            huffmanCodec,
                        ),
                        NpcInfoClient(cache),
                        update.sizeX,
                        update.sizeZ,
                    )
                }
                WorldEntityUpdateType.Idle -> {
                    // noop
                }

                is WorldEntityUpdateType.ActiveV1 -> {
                }
                is WorldEntityUpdateType.LowResolutionToHighResolutionV1 -> {
                    session.allocateWorld(
                        index,
                        PlayerInfoClient(
                            session.localPlayerIndex,
                            huffmanCodec,
                        ),
                        NpcInfoClient(cache),
                        update.sizeX,
                        update.sizeZ,
                    )
                }

                is WorldEntityUpdateType.LowResolutionToHighResolutionV3 -> {
                    session.allocateWorld(
                        index,
                        PlayerInfoClient(
                            session.localPlayerIndex,
                            huffmanCodec,
                        ),
                        NpcInfoClient(cache),
                        update.sizeX,
                        update.sizeZ,
                    )
                }
            }
        }
        return message
    }
}
