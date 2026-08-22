package net.rsprox.proxy.rs3.transcriber

import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.rs3v949.game.incoming.model.unknown.RawUnknownClientPacket
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.unknown.RawUnknownServerPacket

public class Rs3TranscriberPlugin(
    private val transcriber: Rs3Transcriber,
) : Rs3TranscriberRunner {
    private val ifButtonProts: Set<GameClientProt> =
        setOf(
            GameClientProt.IF_BUTTON1,
            GameClientProt.IF_BUTTON2,
            GameClientProt.IF_BUTTON3,
            GameClientProt.IF_BUTTON4,
            GameClientProt.IF_BUTTON5,
            GameClientProt.IF_BUTTON6,
            GameClientProt.IF_BUTTON7,
            GameClientProt.IF_BUTTON8,
            GameClientProt.IF_BUTTON9,
            GameClientProt.IF_BUTTON10,
        )

    private val opNpcProts: Set<GameClientProt> =
        setOf(
            GameClientProt.OPNPC1,
            GameClientProt.OPNPC2,
            GameClientProt.OPNPC3,
            GameClientProt.OPNPC4,
            GameClientProt.OPNPC5,
            GameClientProt.OPNPC6,
        )

    private val opLocProts: Set<GameClientProt> =
        setOf(
            GameClientProt.OPLOC1,
            GameClientProt.OPLOC2,
            GameClientProt.OPLOC3,
            GameClientProt.OPLOC4,
            GameClientProt.OPLOC5,
            GameClientProt.OPLOC6,
        )

    private val opObjProts: Set<GameClientProt> =
        setOf(
            GameClientProt.OPOBJ1,
            GameClientProt.OPOBJ2,
            GameClientProt.OPOBJ3,
            GameClientProt.OPOBJ4,
            GameClientProt.OPOBJ5,
            GameClientProt.OPOBJ6,
        )

    private val opPlayerProts: Set<GameClientProt> =
        setOf(
            GameClientProt.OPPLAYER1,
            GameClientProt.OPPLAYER2,
            GameClientProt.OPPLAYER3,
            GameClientProt.OPPLAYER4,
            GameClientProt.OPPLAYER5,
            GameClientProt.OPPLAYER6,
            GameClientProt.OPPLAYER7,
            GameClientProt.OPPLAYER8,
            GameClientProt.OPPLAYER9,
            GameClientProt.OPPLAYER10,
        )

    private inline fun <reified T> pass(
        value: IncomingMessage,
        block: Rs3Transcriber.(instance: T) -> Unit,
    ) {
        val transcribe = transcriber.onTranscribeStart()
        if (!transcribe) return
        try {
            block(transcriber, value as T)
        } finally {
            transcriber.onTranscribeEnd()
        }
    }

    override fun onClientProt(
        prot: GameClientProt,
        message: IncomingMessage,
    ) {
        if (message is RawUnknownClientPacket) {
            pass(message, Rs3Transcriber::unknownClientOpcode)
            return
        }
        when (prot) {
            in ifButtonProts -> pass(message, Rs3Transcriber::if3Button)
            in opNpcProts -> pass(message, Rs3Transcriber::opNpc)
            in opLocProts -> pass(message, Rs3Transcriber::opLoc)
            in opObjProts -> pass(message, Rs3Transcriber::opObj)
            in opPlayerProts -> pass(message, Rs3Transcriber::opPlayer)
            GameClientProt.EVENT_APPLET_FOCUS_135 -> pass(message, Rs3Transcriber::eventAppletFocus)
            GameClientProt.SEND_NATIVE_MOUSE_CLICK -> pass(message, Rs3Transcriber::eventNativeMouseClick)
            GameClientProt.MOVE_GAMECLICK -> pass(message, Rs3Transcriber::moveGameClick)
            else -> Unit
        }
    }

    override fun onServerPacket(
        prot: GameServerProt,
        message: IncomingMessage,
    ) {
        if (message is RawUnknownServerPacket) {
            pass(message, Rs3Transcriber::unknownServerOpcode)
            return
        }
        when (prot) {
            GameServerProt.VARP_SMALL -> pass(message, Rs3Transcriber::varpSmall)
            GameServerProt.VARP_LARGE -> pass(message, Rs3Transcriber::varpLarge)
            GameServerProt.VARP_LONG -> pass(message, Rs3Transcriber::varpLong)
            GameServerProt.VARBIT_SMALL -> pass(message, Rs3Transcriber::varbitSmall)
            GameServerProt.VARBIT_LARGE -> pass(message, Rs3Transcriber::varbitLarge)
            GameServerProt.IF_OPENTOP -> pass(message, Rs3Transcriber::ifOpenTop)
            GameServerProt.IF_OPENSUB -> pass(message, Rs3Transcriber::ifOpenSub)
            GameServerProt.IF_CLOSESUB -> pass(message, Rs3Transcriber::ifCloseSub)
            GameServerProt.IF_SETHIDE -> pass(message, Rs3Transcriber::ifSetHide)
            GameServerProt.MESSAGE_GAME -> pass(message, Rs3Transcriber::messageGame)
            GameServerProt.NPC_INFO -> pass(message, Rs3Transcriber::npcInfo)
            GameServerProt.PLAYER_INFO -> pass(message, Rs3Transcriber::playerInfo)
            GameServerProt.REBUILD_NORMAL -> pass(message, Rs3Transcriber::rebuildNormal)
            GameServerProt.UPDATE_ZONE_FULL_FOLLOWS,
            GameServerProt.UPDATE_ZONE_PARTIAL_FOLLOWS,
                -> pass(message, Rs3Transcriber::updateZoneFollows)
            GameServerProt.LOC_ADD_CHANGE -> pass(message, Rs3Transcriber::locAddChange)
            GameServerProt.LOC_DEL -> pass(message, Rs3Transcriber::locDel)
            GameServerProt.OBJ_ADD, GameServerProt.OBJ_ADD_BIG -> pass(message, Rs3Transcriber::objAdd)
            GameServerProt.OBJ_DEL, GameServerProt.OBJ_DEL_BIG -> pass(message, Rs3Transcriber::objDel)
            GameServerProt.OBJ_COUNT, GameServerProt.OBJ_COUNT_BIG -> pass(message, Rs3Transcriber::objCount)
            GameServerProt.OBJ_REVEAL, GameServerProt.OBJ_REVEAL_BIG -> pass(message, Rs3Transcriber::objReveal)
            GameServerProt.MAP_ANIM -> pass(message, Rs3Transcriber::mapAnim)
            GameServerProt.MAP_ANIM_V2 -> pass(message, Rs3Transcriber::mapAnimV2)
            GameServerProt.SOUND_AREA -> pass(message, Rs3Transcriber::soundArea)
            GameServerProt.TEXT_COORD -> pass(message, Rs3Transcriber::textCoord)
            GameServerProt.MAP_PROJANIM -> pass(message, Rs3Transcriber::mapProjAnim)
            GameServerProt.MAP_PROJANIM_HALFSQ -> pass(message, Rs3Transcriber::mapProjAnimHalfsq)
            GameServerProt.MAP_PROJANIM_HALFSQ_V2 -> pass(message, Rs3Transcriber::mapProjAnimHalfsqV2)
            GameServerProt.MAP_PROJANIM_V2 -> pass(message, Rs3Transcriber::mapProjAnimV2)
            GameServerProt.UPDATE_ZONE_PARTIAL_ENCLOSED -> pass(message, Rs3Transcriber::updateZonePartialEnclosed)
            GameServerProt.CLIENT_SETVARC_SMALL -> pass(message, Rs3Transcriber::varcSmall)
            GameServerProt.CLIENT_SETVARC_LARGE -> pass(message, Rs3Transcriber::varcLarge)
            GameServerProt.CLIENT_SETVARCBIT_SMALL -> pass(message, Rs3Transcriber::varcBitSmall)
            GameServerProt.CLIENT_SETVARCBIT_LARGE -> pass(message, Rs3Transcriber::varcBitLarge)
            GameServerProt.CLIENT_SETVARCSTR_SMALL -> pass(message, Rs3Transcriber::varcStrSmall)
            GameServerProt.IF_SETNPCHEAD -> pass(message, Rs3Transcriber::ifSetNpcHead)
            GameServerProt.IF_SETPLAYERHEAD -> pass(message, Rs3Transcriber::ifSetPlayerHead)
            GameServerProt.IF_SETTEXT -> pass(message, Rs3Transcriber::ifSetText)
            GameServerProt.IF_SETOBJECT -> pass(message, Rs3Transcriber::ifSetObject)
            GameServerProt.IF_OPENSUB_ACTIVE_OBJ -> pass(message, Rs3Transcriber::ifOpenSubActiveObj)
            GameServerProt.IF_OPENSUB_ACTIVE_LOC -> pass(message, Rs3Transcriber::ifOpenSubActiveLoc)
            GameServerProt.IF_SETMODEL -> pass(message, Rs3Transcriber::ifSetModel)
            GameServerProt.IF_SETPOSITION -> pass(message, Rs3Transcriber::ifSetPosition)
            GameServerProt.IF_SETANIM -> pass(message, Rs3Transcriber::ifSetAnim)
            GameServerProt.IF_SETCOLOUR -> pass(message, Rs3Transcriber::ifSetColour)
            GameServerProt.IF_SETSCROLLPOS -> pass(message, Rs3Transcriber::ifSetScrollPos)
            GameServerProt.IF_SETPLAYERMODEL_SELF -> pass(message, Rs3Transcriber::ifSetPlayerModelSelf)
            GameServerProt.IF_SETTARGETPARAM -> pass(message, Rs3Transcriber::ifSetTargetParam)
            GameServerProt.IF_SETEVENTS -> pass(message, Rs3Transcriber::ifSetEvents)
            GameServerProt.CAM_LOOKAT -> pass(message, Rs3Transcriber::camLookAt)
            GameServerProt.CAM_SHAKE -> pass(message, Rs3Transcriber::camShake)
            GameServerProt.CAM_FORCEANGLE -> pass(message, Rs3Transcriber::camForceAngle)
            GameServerProt.CAM_MOVETO -> pass(message, Rs3Transcriber::camMoveTo)
            GameServerProt.CAMERA_UPDATE -> pass(message, Rs3Transcriber::cameraUpdate)
            GameServerProt.UPDATE_INV_FULL -> pass(message, Rs3Transcriber::updateInvFull)
            GameServerProt.UPDATE_INV_STOP_TRANSMIT -> pass(message, Rs3Transcriber::updateInvStopTransmit)
            GameServerProt.UPDATE_INV_PARTIAL -> pass(message, Rs3Transcriber::updateInvPartial)
            GameServerProt.UPDATE_RUNWEIGHT -> pass(message, Rs3Transcriber::updateRunWeight)
            GameServerProt.UPDATE_STAT -> pass(message, Rs3Transcriber::updateStat)
            GameServerProt.UPDATE_RUNENERGY -> pass(message, Rs3Transcriber::updateRunEnergy)
            GameServerProt.MINIMAP_TOGGLE -> pass(message, Rs3Transcriber::minimapToggle)
            GameServerProt.HINT_TRAIL -> pass(message, Rs3Transcriber::hintTrail)
            GameServerProt.HINT_ARROW -> pass(message, Rs3Transcriber::hintArrow)
            GameServerProt.CHAT_FILTER_SETTINGS_PRIVATECHAT -> pass(message, Rs3Transcriber::chatFilterSettingsPrivateChat)
            GameServerProt.SET_PLAYER_OP -> pass(message, Rs3Transcriber::setPlayerOp)
            GameServerProt.IF_SETPLAYERMODEL_SNAPSHOT -> pass(message, Rs3Transcriber::ifSetPlayerModelSnapshot)
            GameServerProt.IF_SETPLAYERHEAD_SNAPSHOT -> pass(message, Rs3Transcriber::ifSetPlayerHeadSnapshot)
            GameServerProt.SOUND_SYNTH -> pass(message, Rs3Transcriber::soundSynth)
            GameServerProt.RUNCLIENTSCRIPT -> pass(message, Rs3Transcriber::runClientScript)
            GameServerProt.PROJANIM_SPECIFIC_V2 -> pass(message, Rs3Transcriber::projAnimSpecificV2)
            GameServerProt.MIDI_SONG -> pass(message, Rs3Transcriber::midiSong)
            GameServerProt.LOC_PREFETCH -> pass(message, Rs3Transcriber::locPrefetch)
            GameServerProt.CUTSCENE2D_PLAY -> pass(message, Rs3Transcriber::cutscene2dPlay)
            GameServerProt.JCOINS_UPDATE -> pass(message, Rs3Transcriber::jcoinsUpdate)
            GameServerProt.LOBBY_TICK_END, GameServerProt.SERVER_TICK_END -> Unit
            else -> Unit
        }
    }
}
