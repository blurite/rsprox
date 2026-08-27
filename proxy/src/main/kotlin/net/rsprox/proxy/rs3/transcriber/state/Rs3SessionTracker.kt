package net.rsprox.proxy.rs3.transcriber.state

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.rs3v949.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.info.playerinfo.PlayerInfoDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.client.TickEnd
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.UpdateStat
import net.rsprox.protocol.rs3v949.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.header.UpdateZonePartialFollows

public class Rs3SessionTracker(
    private val sessionState: Rs3SessionState,
    private val playerInfoDecoder: PlayerInfoDecoder,
) {
    private fun setCurrentProt(name: String) {
        sessionState.currentProt = name
    }

    public fun onClientPacket(
        @Suppress("UNUSED_PARAMETER") message: IncomingMessage,
        prot: GameClientProt,
    ) {
        setCurrentProt(prot.name)
    }

    public fun onServerPacket(
        @Suppress("UNUSED_PARAMETER") message: IncomingMessage,
        prot: GameServerProt,
    ) {
        setCurrentProt(prot.name)
    }

    public fun beforeTranscribe(message: IncomingMessage) {
        when (message) {
            is NpcInfo -> {
                for ((index, update) in message.updates) {
                    if (update is NpcUpdateType.Add) {
                        sessionState.getActiveWorld().updateNpc(index, Rs3Npc(index, update.id))
                    }
                }
            }
            is PlayerInfo -> {
                for ((index, update) in message.updates) {
                    val old = sessionState.getPlayerOrNull(index)
                    when (update) {
                        is PlayerUpdateType.LowResolutionToHighResolution -> {
                            sessionState.overridePlayer(
                                Rs3Player(index, update.name ?: old?.name, update.level, update.x, update.z),
                            )
                        }
                        is PlayerUpdateType.HighResolutionIdle -> {
                            sessionState.overridePlayer(
                                Rs3Player(index, update.name ?: old?.name, old?.level, old?.x, old?.z),
                            )
                        }
                        is PlayerUpdateType.HighResolutionMovement -> {
                            if (!update.ambiguous) {
                                sessionState.overridePlayer(
                                    Rs3Player(index, update.name ?: old?.name, update.level, update.x, update.z),
                                )
                            } else if (update.name != null && update.name != old?.name) {
                                sessionState.overridePlayer(Rs3Player(index, update.name, old?.level, old?.x, old?.z))
                            }
                        }
                        else -> Unit
                    }
                }
            }
            is RebuildNormal -> {
                val initBlock = message.playerInfoInitBlock
                if (initBlock != null) {
                    playerInfoDecoder.reset()
                    playerInfoDecoder.gpiInit(initBlock)
                }
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
            is TickEnd -> {
                sessionState.incrementCycle()
            }
            else -> Unit
        }
    }

    public fun afterTranscribe(message: IncomingMessage) {
        when (message) {
            is IfOpenTop -> {
                sessionState.toplevelInterface = message.interfaceId
            }
            is IfOpenSub -> {
                sessionState.openInterface(message.childId, message.componentHash)
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
                        is NpcUpdateType.Active, NpcUpdateType.Idle, is NpcUpdateType.Add -> Unit
                    }
                }
            }

            is UpdateStat -> {
                sessionState.setExperience(message.skillId, message.xp.toLong())
            }
        }
    }
}
