package net.rsprox.proxy.rs3.transcriber.state

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo.NpcMask
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveLoc
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveNpc
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActiveObj
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenSubActivePlayer
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.TickEnd
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateStat
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.rs3v949.game.outgoing.model.map.RebuildNormal as LegacyRebuildNormal

public class Rs3SessionTracker(
    private val sessionState: Rs3SessionState,
) {
    private fun setCurrentProt(name: String) {
        sessionState.currentProt = name
    }

    public fun onClientPacket(
        @Suppress("UNUSED_PARAMETER") message: IncomingMessage,
        prot: ClientProt,
    ) {
        setCurrentProt(prot.toString())
    }

    public fun onServerPacket(
        @Suppress("UNUSED_PARAMETER") message: IncomingMessage,
        prot: ClientProt,
    ) {
        setCurrentProt(prot.toString())
    }

    public fun beforeTranscribe(message: IncomingMessage) {
        when (message) {
            is NpcInfo -> {
                for ((index, update) in message.updates) {
                    if (update is NpcUpdateType.Add) {
                        sessionState.getActiveWorld().updateNpc(
                            index,
                            Rs3Npc(
                                index,
                                update.id,
                                coord = CoordGrid(update.level, update.x, update.z),
                                spawnAngle = (1024 - update.direction * 256) and 2047,
                            ),
                        )
                    }
                }
            }
            is PlayerInfo -> {
                // Publish additions before transcription; movement is committed afterwards,
                // so references in the transcript still describe the source position.
                for ((index, update) in message.updates) {
                    if (update is PlayerUpdateType.LowResolutionToHighResolution) {
                        val name = sessionState.getPlayerOrNull(index)?.name
                        sessionState.overridePlayer(Rs3Player(index, name, update.level, update.x, update.z))
                    }
                    val masks = when (update) {
                        is PlayerUpdateType.LowResolutionToHighResolution -> update.extendedInfo
                        is PlayerUpdateType.HighResolutionMovement -> update.extendedInfo
                        is PlayerUpdateType.HighResolutionIdle -> update.extendedInfo
                        else -> emptyList()
                    }
                    val appearance = masks.filterIsInstance<PlayerExtendedInfo.Appearance>().lastOrNull()
                    if (appearance != null) {
                        val player = sessionState.getPlayerOrNull(index) ?: Rs3Player(index)
                        sessionState.overridePlayer(player.copy(name = appearance.name))
                    }
                }
            }
            is RebuildNormal, is RebuildRegion -> {
                val init = when (message) {
                    is RebuildNormal -> message.playerInfoInit
                    is RebuildRegion -> message.playerInfoInit
                    else -> null
                }
                if (init != null) {
                    sessionState.clearPlayers()
                    sessionState.localPlayerIndex = init.localPlayerIndex
                    sessionState.overridePlayer(
                        Rs3Player(init.localPlayerIndex, null, init.localPlayerLevel, init.localPlayerX, init.localPlayerZ),
                    )
                }
                when (message) {
                    is RebuildNormal -> {
                        sessionState.getActiveWorld().rebuild(message)
                    }
                    is RebuildRegion -> {
                        sessionState.getActiveWorld().rebuild(message)
                    }
                }
            }
            is LegacyRebuildNormal -> {
                if (message.trailerAligned) {
                    sessionState.getActiveWorld().rebuild(CoordGrid(0, message.baseTileX, message.baseTileZ))
                }
            }
            is UpdateZoneFullFollows -> {
                sessionState.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
            }
            is UpdateZonePartialFollows -> {
                sessionState.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
            }
            is UpdateZonePartialEnclosed -> {
                sessionState.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
            }
            else -> Unit
        }
    }

    public fun afterTranscribe(message: IncomingMessage) {
        when (message) {
            is PlayerInfo -> {
                for ((index, update) in message.updates) {
                    when (update) {
                        is PlayerUpdateType.HighResolutionMovement -> {
                            val name = sessionState.getPlayerOrNull(index)?.name
                            sessionState.overridePlayer(Rs3Player(index, name, update.level, update.x, update.z))
                        }
                        is PlayerUpdateType.HighResolutionToLowResolution -> sessionState.removePlayer(index)
                        else -> Unit
                    }
                }
            }
            is TickEnd -> {
                sessionState.incrementCycle()
            }
            is IfOpenTop -> {
                sessionState.toplevelInterface = message.interfaceId
            }
            is IfOpenSub -> {
                sessionState.openInterface(message.childId, message.componentHash)
            }
            is IfOpenSubActiveNpc -> {
                sessionState.openInterface(message.childId, message.componentHash)
            }
            is IfOpenSubActivePlayer -> {
                sessionState.openInterface(message.childId, message.componentHash)
            }
            is IfOpenSubActiveObj -> {
                sessionState.openInterface(message.childId, message.componentHash)
            }
            is IfOpenSubActiveLoc -> {
                sessionState.openInterface(message.childId, message.componentHash)
            }
            is IfMoveSub -> {
                sessionState.moveInterface(message.source, message.destination)
            }
            is IfCloseSub -> {
                sessionState.closeInterface(message.parentComponentHash)
            }

            is NpcInfo -> {
                for ((index, update) in message.updates) {
                    when (update) {
                        NpcUpdateType.Remove -> {
                            sessionState.getActiveWorld().removeNpc(index)
                        }
                        is NpcUpdateType.Active, is NpcUpdateType.Add -> {
                            val world = sessionState.getActiveWorld()
                            if (update is NpcUpdateType.Active) {
                                world.getNpcOrNull(index)?.let { npc ->
                                    world.updateNpc(index, npc.copy(coord = CoordGrid(update.level, update.x, update.z)))
                                }
                            }
                            val masks = when (update) {
                                is NpcUpdateType.Active -> update.extendedInfo
                                is NpcUpdateType.Add -> update.extendedInfo
                                else -> error("Not an active NPC update")
                            }
                            masks.filterIsInstance<NpcMask.Transformation>().lastOrNull()?.let {
                                val npc = world.getNpcOrNull(index) ?: Rs3Npc(index, it.id)
                                world.updateNpc(index, npc.copy(id = it.id, name = null))
                            }
                        }
                        NpcUpdateType.Idle -> Unit
                    }
                }
            }

            is UpdateStat -> {
                sessionState.setExperience(message.skillId, message.xp.toLong())
            }
        }
    }
}
