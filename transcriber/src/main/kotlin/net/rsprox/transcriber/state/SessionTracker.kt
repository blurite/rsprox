package net.rsprox.transcriber.state

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.cache.api.Cache
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.TransformationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.AppearanceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.MoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.TemporaryMoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityUpdateType
import net.rsprox.protocol.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.game.outgoing.model.interfaces.IfResync
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.protocol.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV1
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV2
import net.rsprox.protocol.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV1
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV2
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.game.outgoing.model.worldentity.ClearEntities
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.shared.SessionMonitor
import net.rsprox.transcriber.firstOfInstanceOfNull
import net.rsprox.transcriber.prot.GameClientProt
import net.rsprox.transcriber.prot.GameServerProt
import net.rsprox.transcriber.prot.Prot

public class SessionTracker(
    private val sessionState: SessionState,
    private val cache: Cache,
    private val monitor: SessionMonitor<*>,
) {
    private fun setCurrentProt(prot: Prot) {
        sessionState.currentProt = prot.toString()
    }

    public fun onServerPacket(
        message: IncomingMessage,
        prot: net.rsprot.protocol.Prot,
    ) {
        if (message is RebuildLogin) {
            setCurrentProt(GameServerProt.REBUILD_NORMAL)
            return
        }
        val toString = prot.toString()
        val serverProt = GameServerProt.valueOf(toString)
        setCurrentProt(serverProt)
    }

    public fun onClientPacket(
        @Suppress("UNUSED_PARAMETER") message: IncomingMessage,
        prot: net.rsprot.protocol.Prot,
    ) {
        val toString = prot.toString()
        val clientProt = GameClientProt.valueOf(toString)
        setCurrentProt(clientProt)
    }

    public fun beforeTranscribe(message: IncomingMessage) {
        when (message) {
            is RebuildNormal -> {
                val world = sessionState.getWorld(-1)
                world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))
                world.setBuildArea(null)
            }
            is RebuildRegion -> {
                val world = sessionState.getWorld(-1)
                world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))
                world.setBuildArea(message.buildArea)
            }
            is RebuildLogin -> {
                sessionState.overridePlayer(
                    Player(
                        message.playerInfoInitBlock.localPlayerIndex,
                        "uninitialized",
                        message.playerInfoInitBlock.localPlayerCoord,
                    ),
                )
                sessionState.localPlayerIndex = message.playerInfoInitBlock.localPlayerIndex
                val world = sessionState.createWorld(-1)
                world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))
                world.setBuildArea(null)
            }
            is RebuildWorldEntityV1 -> {
                val world = sessionState.getWorld(message.index)
                world.rebuild(CoordGrid(0, (message.baseX - 6) shl 3, (message.baseZ - 6) shl 3))
                world.setBuildArea(message.buildArea)
            }
            is RebuildWorldEntityV2 -> {
                val world = sessionState.getWorld(message.index)
                world.rebuild(CoordGrid(0, (message.baseX - 6) shl 3, (message.baseZ - 6) shl 3))
                world.setBuildArea(message.buildArea)
            }
            is VarpSmall -> {
                if (!sessionState.varbitsLoaded()) {
                    sessionState.associateVarbits(cache.listVarBitTypes())
                }
            }
            is VarpLarge -> {
                if (!sessionState.varbitsLoaded()) {
                    sessionState.associateVarbits(cache.listVarBitTypes())
                }
            }
            ClearEntities -> {
                sessionState.destroyDynamicWorlds()
            }
            is UpdateZoneFullFollows -> {
                sessionState.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
            }
            is UpdateZonePartialEnclosed -> {
                sessionState.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
            }
            is UpdateZonePartialFollows -> {
                sessionState.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
            }
            is WorldEntityInfo -> {
                for ((index, update) in message.updates) {
                    when (update) {
                        is WorldEntityUpdateType.ActiveV2 -> {
                        }
                        WorldEntityUpdateType.HighResolutionToLowResolution -> {
                        }
                        is WorldEntityUpdateType.LowResolutionToHighResolutionV2 -> {
                            val world = sessionState.createWorld(index)
                            world.sizeX = update.sizeX
                            world.sizeZ = update.sizeZ
                            world.angle = update.angle
                            world.level = update.level
                            world.centerFineOffsetX = update.centerFineOffsetX
                            world.centerFineOffsetZ = update.centerFineOffsetZ
                            world.coordFine = update.coordFine
                            world.coord = update.coordFine.toCoordGrid(world.level)
                        }
                        WorldEntityUpdateType.Idle -> {
                            // noop
                        }

                        is WorldEntityUpdateType.ActiveV1 -> {
                        }
                        is WorldEntityUpdateType.LowResolutionToHighResolutionV1 -> {
                            val world = sessionState.createWorld(index)
                            world.sizeX = update.sizeX
                            world.sizeZ = update.sizeZ
                            world.angle = update.angle
                            // world.unknownProperty = update.unknownProperty
                            world.coord = update.coordGrid
                        }
                    }
                }
            }
            is PlayerInfo -> {
                sessionState.clearTempMoveSpeeds()
                for ((index, update) in message.updates) {
                    when (update) {
                        is PlayerUpdateType.LowResolutionToHighResolution -> {
                            val name = loadPlayerName(index, update.extendedInfo)
                            sessionState.overridePlayer(Player(index, name, update.coord))
                            preprocessExtendedInfo(index, update.extendedInfo)
                        }
                        is PlayerUpdateType.HighResolutionIdle -> {
                            val name = loadPlayerName(index, update.extendedInfo)
                            val player = sessionState.getPlayer(index)
                            sessionState.overridePlayer(Player(index, name, player.coord))
                            preprocessExtendedInfo(index, update.extendedInfo)
                        }
                        is PlayerUpdateType.HighResolutionMovement -> {
                            val name = loadPlayerName(index, update.extendedInfo)
                            val player = sessionState.getPlayer(index)
                            sessionState.overridePlayer(Player(index, name, player.coord))
                            preprocessExtendedInfo(index, update.extendedInfo)
                        }
                        else -> {
                            // No-op, no info to preload
                        }
                    }
                }
            }
            is NpcInfo -> {
                val world = sessionState.getActiveWorld()
                for ((index, update) in message.updates) {
                    when (update) {
                        is NpcUpdateType.Active -> {
                        }
                        NpcUpdateType.HighResolutionToLowResolution -> {
                        }
                        is NpcUpdateType.LowResolutionToHighResolution -> {
                            val name = cache.getNpcType(update.id)?.name
                            world.createNpc(
                                index,
                                update.id,
                                name,
                                update.spawnCycle,
                                CoordGrid(update.level, update.x, update.z),
                            )
                        }
                        NpcUpdateType.Idle -> {
                            // noop
                        }
                    }
                }
            }
        }
    }

    private fun loadPlayerName(
        index: Int,
        extendedInfo: List<ExtendedInfo>,
    ): String {
        val appearance =
            extendedInfo
                .filterIsInstance<AppearanceExtendedInfo>()
                .singleOrNull()
        return appearance?.name
            ?: sessionState.getLastKnownPlayerName(index)
            ?: "null"
    }

    private fun preprocessExtendedInfo(
        index: Int,
        extendedInfo: List<ExtendedInfo>,
    ) {
        val moveSpeed = extendedInfo.firstOfInstanceOfNull<MoveSpeedExtendedInfo>()
        if (moveSpeed != null) {
            sessionState.setCachedMoveSpeed(index, moveSpeed.speed)
        }
        val tempMoveSpeed = extendedInfo.firstOfInstanceOfNull<TemporaryMoveSpeedExtendedInfo>()
        if (tempMoveSpeed != null) {
            sessionState.setTempMoveSpeed(index, tempMoveSpeed.speed)
        }
        if (index == sessionState.localPlayerIndex) {
            val appearance = extendedInfo.firstOfInstanceOfNull<AppearanceExtendedInfo>()
            if (appearance != null) {
                monitor.onNameUpdate(appearance.name)
            }
        }
    }

    public fun afterTranscribe(message: IncomingMessage) {
        when (message) {
            is WorldEntityInfo -> {
                for ((index, update) in message.updates) {
                    when (update) {
                        is WorldEntityUpdateType.ActiveV2 -> {
                            val world = sessionState.getWorld(index)
                            world.angle = update.angle
                            world.coordFine = update.coordFine
                            world.coord = update.coordFine.toCoordGrid(world.level)
                        }
                        WorldEntityUpdateType.HighResolutionToLowResolution -> {
                            sessionState.destroyWorld(index)
                        }
                        is WorldEntityUpdateType.LowResolutionToHighResolutionV2 -> {
                        }
                        WorldEntityUpdateType.Idle -> {
                            // noop
                        }
                        is WorldEntityUpdateType.ActiveV1 -> {
                            val world = sessionState.getWorld(index)
                            world.angle = update.angle
                            world.coord = update.coordGrid
                            // world.moveSpeed = update.moveSpeed
                        }
                        is WorldEntityUpdateType.LowResolutionToHighResolutionV1 -> {
                        }
                    }
                }
            }
            is IfCloseSub -> {
                sessionState.closeInterface(message.combinedId)
            }
            is IfMoveSub -> {
                sessionState.moveInterface(message.sourceCombinedId, message.destinationCombinedId)
            }
            is IfOpenSub -> {
                sessionState.openInterface(message.interfaceId, message.destinationCombinedId)
            }
            is IfOpenTop -> {
                sessionState.toplevelInterface = message.interfaceId
            }
            is IfResync -> {
                sessionState.toplevelInterface = message.topLevelInterface
                for (sub in message.subInterfaces) {
                    sessionState.openInterface(sub.interfaceId, sub.destinationCombinedId)
                }
            }
            ServerTickEnd -> {
                sessionState.incrementCycle()
            }
            is UpdateStatV2 -> {
                sessionState.setExperience(message.stat, message.experience)
            }
            is UpdateStatV1 -> {
                sessionState.setExperience(message.stat, message.experience)
            }
            is VarpSmall -> {
                sessionState.setVarp(message.id, message.value)
            }
            is VarpLarge -> {
                sessionState.setVarp(message.id, message.value)
            }
            is Reconnect -> {
                sessionState.lastConnection = sessionState.cycle
            }
            is PlayerInfo -> {
                for ((index, update) in message.updates) {
                    when (update) {
                        is PlayerUpdateType.LowResolutionToHighResolution -> {
                            val name = loadPlayerName(index, update.extendedInfo)
                            sessionState.overridePlayer(Player(index, name, update.coord))
                        }
                        is PlayerUpdateType.HighResolutionIdle -> {
                            val oldPlayer = sessionState.getPlayerOrNull(index) ?: return
                            val name = loadPlayerName(index, update.extendedInfo)
                            sessionState.overridePlayer(Player(index, name, oldPlayer.coord))
                        }
                        is PlayerUpdateType.HighResolutionMovement -> {
                            val name = loadPlayerName(index, update.extendedInfo)
                            sessionState.overridePlayer(Player(index, name, update.coord))
                        }
                        else -> {
                            // No-op, no info to preload
                        }
                    }
                }
            }
            is NpcInfo -> {
                val world = sessionState.getActiveWorld()
                for ((index, update) in message.updates) {
                    when (update) {
                        is NpcUpdateType.Active -> {
                            world.updateNpc(index, CoordGrid(update.level, update.x, update.z))
                            val blocks = update.extendedInfo.filterIsInstance<TransformationExtendedInfo>()
                            if (blocks.isNotEmpty()) {
                                val npc = world.getNpc(index)
                                val transform = blocks.single()
                                world.updateNpcName(npc.index, cache.getNpcType(transform.id)?.name)
                            }
                        }
                        NpcUpdateType.HighResolutionToLowResolution -> {
                            world.removeNpc(index)
                        }
                        is NpcUpdateType.LowResolutionToHighResolution -> {
                        }
                        NpcUpdateType.Idle -> {
                            // noop
                        }
                    }
                }
            }
        }
    }
}
