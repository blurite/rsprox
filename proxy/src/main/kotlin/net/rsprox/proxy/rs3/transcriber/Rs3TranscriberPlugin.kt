package net.rsprox.proxy.rs3.transcriber

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.incoming.model.unknown.UnknownClientPacket
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownServerPacket

public class Rs3TranscriberPlugin(
    private val transcriber: Rs3Transcriber,
) : Rs3TranscriberRunner {
    private val ifButtonProtNames: Set<String> =
        setOf(
            "IF_BUTTON1_V2", "IF_BUTTON2_V2", "IF_BUTTON3_V2", "IF_BUTTON4_V2", "IF_BUTTON5_V2",
            "IF_BUTTON6_V2", "IF_BUTTON7_V2", "IF_BUTTON8_V2", "IF_BUTTON9_V2", "IF_BUTTON10_V2",
        )

    private val opNpcProtNames: Set<String> =
        setOf("OPNPC1", "OPNPC2", "OPNPC3", "OPNPC4", "OPNPC5", "OPNPC6")

    private val opLocProtNames: Set<String> =
        setOf("OPLOC1", "OPLOC2", "OPLOC3", "OPLOC4", "OPLOC5", "OPLOC6")

    private val opObjProtNames: Set<String> =
        setOf("OPOBJ1_V2", "OPOBJ2_V2", "OPOBJ3_V2", "OPOBJ4_V2", "OPOBJ5_V2", "OPOBJ6_V2")

    private val opPlayerProtNames: Set<String> =
        setOf(
            "OPPLAYER1", "OPPLAYER2", "OPPLAYER3", "OPPLAYER4", "OPPLAYER5",
            "OPPLAYER6", "OPPLAYER7", "OPPLAYER8", "OPPLAYER9", "OPPLAYER10",
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

    override fun onClientProt(prot: ClientProt, message: IncomingMessage) {
        if (message is UnknownClientPacket) {
            pass(message, Rs3Transcriber::unknownClientOpcode)
            return
        }
        val name = prot.toString()
        when {
            name in ifButtonProtNames -> pass(message, Rs3Transcriber::if3Button)
            name in opNpcProtNames -> pass(message, Rs3Transcriber::opNpc)
            name in opLocProtNames -> pass(message, Rs3Transcriber::opLoc)
            name in opObjProtNames -> pass(message, Rs3Transcriber::opObj)
            name in opPlayerProtNames -> pass(message, Rs3Transcriber::opPlayer)
            name == "EVENT_APPLET_FOCUS" || name == "EVENT_APPLET_FOCUS_135" ->
                pass(message, Rs3Transcriber::eventAppletFocus)
            name == "EVENT_NATIVE_MOUSE_CLICK" -> pass(message, Rs3Transcriber::eventNativeMouseClick)
            name == "MOVE_GAMECLICK" -> pass(message, Rs3Transcriber::moveGameClick)
            else -> Unit
        }
    }

    override fun onServerPacket(prot: ClientProt, message: IncomingMessage) {
        if (message is UnknownServerPacket) {
            pass(message, Rs3Transcriber::unknownServerOpcode)
            return
        }
        when (prot.toString()) {
            "VARP_SMALL" -> pass(message, Rs3Transcriber::varpSmall)
            "VARP_LARGE" -> pass(message, Rs3Transcriber::varpLarge)
            "VARP_LONG" -> pass(message, Rs3Transcriber::varpLong)
            "VARBIT_SMALL" -> pass(message, Rs3Transcriber::varbitSmall)
            "VARBIT_LARGE" -> pass(message, Rs3Transcriber::varbitLarge)
            "IF_OPENTOP" -> pass(message, Rs3Transcriber::ifOpenTop)
            "IF_OPENSUB" -> pass(message, Rs3Transcriber::ifOpenSub)
            "IF_CLOSESUB" -> pass(message, Rs3Transcriber::ifCloseSub)
            "IF_SETHIDE" -> pass(message, Rs3Transcriber::ifSetHide)
            "MESSAGE_GAME" -> pass(message, Rs3Transcriber::messageGame)
            "NPC_INFO_V2" -> pass(message, Rs3Transcriber::npcInfo)
            "PLAYER_INFO" -> pass(message, Rs3Transcriber::playerInfo)
            "REBUILD_NORMAL" -> pass(message, Rs3Transcriber::rebuildNormal)
            "UPDATE_ZONE_FULL_FOLLOWS" -> pass(message, Rs3Transcriber::updateZoneFullFollows)
            "UPDATE_ZONE_PARTIAL_FOLLOWS" -> pass(message, Rs3Transcriber::updateZonePartialFollows)
            "LOC_ANIM" -> pass(message, Rs3Transcriber::locAnim)
            "LOC_ADD_CHANGE" -> pass(message, Rs3Transcriber::locAddChange)
            "LOC_DEL" -> pass(message, Rs3Transcriber::locDel)
            "OBJ_ADD", "OBJ_ADD_V2" -> pass(message, Rs3Transcriber::objAdd)
            "OBJ_DEL", "OBJ_DEL_V2" -> pass(message, Rs3Transcriber::objDel)
            "OBJ_COUNT", "OBJ_COUNT_V2" -> pass(message, Rs3Transcriber::objCount)
            "OBJ_REVEAL", "OBJ_REVEAL_V2" -> pass(message, Rs3Transcriber::objReveal)
            "MAP_ANIM" -> pass(message, Rs3Transcriber::mapAnim)
            "MAP_ANIM_V2" -> pass(message, Rs3Transcriber::mapAnimV2)
            "MIDI_SONG_LOCATION" -> pass(message, Rs3Transcriber::midiSongLocation)
            "SOUND_AREA" -> pass(message, Rs3Transcriber::soundArea)
            "TEXT_COORD" -> pass(message, Rs3Transcriber::textCoord)
            "MAP_PROJANIM" -> pass(message, Rs3Transcriber::mapProjAnim)
            "MAP_PROJANIM_HALFSQ" -> pass(message, Rs3Transcriber::mapProjAnimHalfsq)
            "MAP_PROJANIM_HALFSQ_V2" -> pass(message, Rs3Transcriber::mapProjAnimHalfsqV2)
            "MAP_PROJANIM_V2" -> pass(message, Rs3Transcriber::mapProjAnimV2)
            "UPDATE_ZONE_PARTIAL_ENCLOSED" -> pass(message, Rs3Transcriber::updateZonePartialEnclosed)
            "CLIENT_SETVARC_SMALL" -> pass(message, Rs3Transcriber::varcSmall)
            "CLIENT_SETVARC_LARGE" -> pass(message, Rs3Transcriber::varcLarge)
            "CLIENT_SETVARCBIT_SMALL" -> pass(message, Rs3Transcriber::varcBitSmall)
            "CLIENT_SETVARCBIT_LARGE" -> pass(message, Rs3Transcriber::varcBitLarge)
            "CLIENT_SETVARCSTR_SMALL" -> pass(message, Rs3Transcriber::varcStrSmall)
            "IF_SETNPCHEAD" -> pass(message, Rs3Transcriber::ifSetNpcHead)
            "IF_SETPLAYERHEAD" -> pass(message, Rs3Transcriber::ifSetPlayerHead)
            "IF_SETTEXT" -> pass(message, Rs3Transcriber::ifSetText)
            "IF_SETOBJECT_V2" -> pass(message, Rs3Transcriber::ifSetObject)
            "IF_OPEN_SUB_ACTIVE_OBJ", "IF_OPENSUB_ACTIVE_OBJ_V2" -> pass(message, Rs3Transcriber::ifOpenSubActiveObj)
            "IF_OPENSUB_ACTIVE_LOC" -> pass(message, Rs3Transcriber::ifOpenSubActiveLoc)
            "IF_SETMODEL" -> pass(message, Rs3Transcriber::ifSetModel)
            "IF_SETPOSITION" -> pass(message, Rs3Transcriber::ifSetPosition)
            "IF_SETANIM" -> pass(message, Rs3Transcriber::ifSetAnim)
            "IF_SETCOLOUR" -> pass(message, Rs3Transcriber::ifSetColour)
            "IF_SETSCROLLPOS" -> pass(message, Rs3Transcriber::ifSetScrollPos)
            "IF_SETPLAYERMODEL_SELF" -> pass(message, Rs3Transcriber::ifSetPlayerModelSelf)
            "IF_SETTARGETPARAM" -> pass(message, Rs3Transcriber::ifSetTargetParam)
            "IF_SETEVENTS" -> pass(message, Rs3Transcriber::ifSetEvents)
            "CAM_LOOKAT" -> pass(message, Rs3Transcriber::camLookAt)
            "CAM_SHAKE" -> pass(message, Rs3Transcriber::camShake)
            "CAM_FORCEANGLE" -> pass(message, Rs3Transcriber::camForceAngle)
            "CAM_MOVETO" -> pass(message, Rs3Transcriber::camMoveTo)
            "CAMERA_UPDATE" -> pass(message, Rs3Transcriber::cameraUpdate)
            "UPDATE_INV_FULL" -> pass(message, Rs3Transcriber::updateInvFull)
            "UPDATE_INV_STOP_TRANSMIT" -> pass(message, Rs3Transcriber::updateInvStopTransmit)
            "UPDATE_INV_PARTIAL" -> pass(message, Rs3Transcriber::updateInvPartial)
            "UPDATE_RUNWEIGHT" -> pass(message, Rs3Transcriber::updateRunWeight)
            "UPDATE_STAT" -> pass(message, Rs3Transcriber::updateStat)
            "UPDATE_RUNENERGY" -> pass(message, Rs3Transcriber::updateRunEnergy)
            "MINIMAP_TOGGLE" -> pass(message, Rs3Transcriber::minimapToggle)
            "HINT_TRAIL" -> pass(message, Rs3Transcriber::hintTrail)
            "HINT_ARROW" -> pass(message, Rs3Transcriber::hintArrow)
            "CHAT_FILTER_SETTINGS_PRIVATECHAT" -> pass(message, Rs3Transcriber::chatFilterSettingsPrivateChat)
            "SET_PLAYER_OP" -> pass(message, Rs3Transcriber::setPlayerOp)
            "IF_SETPLAYERMODEL_SNAPSHOT" -> pass(message, Rs3Transcriber::ifSetPlayerModelSnapshot)
            "IF_SETPLAYERHEAD_SNAPSHOT" -> pass(message, Rs3Transcriber::ifSetPlayerHeadSnapshot)
            "VORBIS_SOUND" -> pass(message, Rs3Transcriber::vorbisSound)
            "RUNCLIENTSCRIPT" -> pass(message, Rs3Transcriber::runClientScript)
            "PROJANIM_SPECIFIC_V2" -> pass(message, Rs3Transcriber::projAnimSpecificV2)
            "LOC_PREFETCH" -> pass(message, Rs3Transcriber::locPrefetch)
            "CUTSCENE2D_PLAY" -> pass(message, Rs3Transcriber::cutscene2dPlay)
            "JCOINS_UPDATE" -> pass(message, Rs3Transcriber::jcoinsUpdate)
            "NO_TIMEOUT", "SERVER_TICK_END" -> Unit
            else -> Unit
        }
    }
}
