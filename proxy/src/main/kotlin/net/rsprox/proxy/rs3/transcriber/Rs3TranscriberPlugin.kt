package net.rsprox.proxy.rs3.transcriber

import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivate
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivateEcho
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatClanchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatFriendchat
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPlayerGroup
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.Varclan

import net.rsprox.protocol.rs3.game.outgoing.model.appearance.LobbyAppearance
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.PlayerSnapshot

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.IncomingMessage
import net.rsprox.protocol.game.incoming.model.unknown.UnknownClientPacket
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownServerPacket
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePrivate as ClientMessagePrivate
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePublic as ClientMessagePublic
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.NoTimeout as ClientNoTimeout
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CameraUpdate
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamForceAngle
import net.rsprox.protocol.rs3.game.outgoing.model.camera.CamShake
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintTrail
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.NoTimeout
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetPlayerOp
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePublic
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnim
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.LocCustomise
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimHalfsq
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimHalfsqV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MapProjAnimV2
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.MidiSongLocation
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.SoundAreaV1
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.SoundAreaV2

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
        } catch (exception: Exception) {
            transcriber.onTranscribeFailure(exception)
            throw exception
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
            name == "MOVE_MINIMAPCLICK" -> pass(message, Rs3Transcriber::moveMinimapClick)
            name == "MOVE_SCRIPTED" -> pass(message, Rs3Transcriber::moveScripted)
            name == "OPNPCT_V2" -> pass(message, Rs3Transcriber::opNpcT)
            name == "OPPLAYERT_V2" -> pass(message, Rs3Transcriber::opPlayerT)
            name == "OPLOCT_V2" -> pass(message, Rs3Transcriber::opLocT)
            name == "OPOBJT_V2" -> pass(message, Rs3Transcriber::opObjT)
            name == "APCOORDT_V2" -> pass(message, Rs3Transcriber::apCoordT)
            name == "IF_BUTTONT_V2" -> pass(message, Rs3Transcriber::ifButtonT)
            name == "IF_BUTTOND_V2" -> pass(message, Rs3Transcriber::ifButtonD)
            name == "EVENT_CAMERA_POSITION" -> pass(message, Rs3Transcriber::eventCameraPosition)
            name == "WINDOW_STATUS" -> pass(message, Rs3Transcriber::windowStatus)
            name == "RESUME_PAUSEBUTTON" -> pass(message, Rs3Transcriber::resumePauseButton)
            name == "RESUME_P_COUNTDIALOG" -> pass(message, Rs3Transcriber::resumePCountDialog)
            name == "RESUME_P_COUNTDIALOG_LONG" -> pass(message, Rs3Transcriber::resumePCountDialogLong)
            name == "RESUME_P_HSLDIALOG" -> pass(message, Rs3Transcriber::resumePHslDialog)
            name == "RESUME_P_OBJDIALOG" -> pass(message, Rs3Transcriber::resumePObjDialog)
            name == "RESUME_P_STRINGDIALOG" -> pass(message, Rs3Transcriber::resumePStringDialog)
            name == "RESUME_P_NAMEDIALOG" -> pass(message, Rs3Transcriber::resumePNameDialog)
            name == "RESUME_P_CLANFORUMQFCDIALOG" -> pass(message, Rs3Transcriber::resumePClanForumQfcDialog)
            name == "MESSAGE_PUBLIC" -> pass<ClientMessagePublic>(message, Rs3Transcriber::messagePublic)
            name == "MESSAGE_PRIVATE" -> pass<ClientMessagePrivate>(message, Rs3Transcriber::messagePrivate)
            name == "CLIENT_CHEAT" -> pass(message, Rs3Transcriber::clientCheat)
            name == "CHAT_SETMODE" -> pass(message, Rs3Transcriber::chatSetMode)
            name == "SET_CHATFILTERSETTINGS" -> pass(message, Rs3Transcriber::setChatFilterSettings)
            name == "MAP_BUILD_COMPLETE" -> pass(message, Rs3Transcriber::mapBuildComplete)
            name == "CLOSE_MODAL" -> pass(message, Rs3Transcriber::closeModal)
            name == "ABORT_P_DIALOG" -> pass(message, Rs3Transcriber::abortPDialog)
            name == "NO_TIMEOUT" -> pass<ClientNoTimeout>(message, Rs3Transcriber::noTimeout)
            name == "MAP_BUILD_COMPLETE_V2" -> pass(message, Rs3Transcriber::mapBuildCompleteV2)
            name == "IF_CRM_BUTTON" ->
                pass(message, Rs3Transcriber::ifCrmButton)
            name == "IGNORELIST_DEL" ->
                pass(message, Rs3Transcriber::ignoreListDel)
            name == "FRIENDLIST_ADD" ->
                pass(message, Rs3Transcriber::friendListAdd)
            name == "CLANCHANNEL_KICKUSER" ->
                pass(message, Rs3Transcriber::clanChannelKickUser)
            name == "CLICKWORLDMAP" ->
                pass(message, Rs3Transcriber::clickWorldMap)
            name == "BUG_REPORT" ->
                pass(message, Rs3Transcriber::bugReport)
            name == "IF_CRMVIEW_OP" ->
                pass(message, Rs3Transcriber::ifCrmViewOp)
            name == "CUTSCENE2D_FINISHED" ->
                pass(message, Rs3Transcriber::cutscene2DFinished)
            name == "TRANSMITVAR_VERIFYID" ->
                pass(message, Rs3Transcriber::transmitVarVerifyId)
            name == "PING_STATISTICS" ->
                pass(message, Rs3Transcriber::pingStatistics)
            name == "SEND_EMAIL_VALIDATION_CODE" ->
                pass(message, Rs3Transcriber::sendEmailValidationCode)
            name == "FRIENDLIST_DEL" ->
                pass(message, Rs3Transcriber::friendListDel)
            name == "FRIENDCHAT_KICK" ->
                pass(message, Rs3Transcriber::friendChatKick)
            name == "AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL" ->
                pass(message, Rs3Transcriber::affinedClanSettingsSetMutedFromChannel)
            name == "FACE_SQUARE" ->
                pass(message, Rs3Transcriber::faceSquare)
            name == "URL_REQUEST" ->
                pass(message, Rs3Transcriber::urlRequest)
            name == "EVENT_MOUSE_CLICK" ->
                pass(message, Rs3Transcriber::eventMouseClick)
            name == "FRIENDCHAT_JOIN_LEAVE" ->
                pass(message, Rs3Transcriber::friendChatJoinLeave)
            name == "CREATE_LOG_PROGRESS" ->
                pass(message, Rs3Transcriber::createLogProgress)
            name == "IGNORE_SETNOTES" ->
                pass(message, Rs3Transcriber::ignoreSetNotes)
            name == "AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL" ->
                pass(message, Rs3Transcriber::affinedClanSettingsAddBannedFromChannel)
            name == "FRIEND_SETNOTES" ->
                pass(message, Rs3Transcriber::friendSetNotes)
            name == "SEND_PING_REPLY" ->
                pass(message, Rs3Transcriber::sendPingReply)
            name == "FRIENDCHAT_SETRANK" ->
                pass(message, Rs3Transcriber::friendChatSetRank)
            name == "WORLDLIST_FETCH" ->
                pass(message, Rs3Transcriber::worldListFetch)
            name == "SOUND_SONGEND" ->
                pass(message, Rs3Transcriber::soundSongEnd)
            name == "IGNORELIST_ADD" ->
                pass(message, Rs3Transcriber::ignoreListAdd)
            name == "IF_TEXT_CHANGE" ->
                pass(message, Rs3Transcriber::ifTextChange)
            name == "IF_VALUE_CHANGE_32" ->
                pass(message, Rs3Transcriber::ifValueChange32)
            name == "LOCSELECT_SUBMIT" ->
                pass(message, Rs3Transcriber::locSelectSubmit)
            name == "EVENT_KEYBOARD" -> pass(message, Rs3Transcriber::eventKeyboard)
            name == "EVENT_MOUSE_MOVE" -> pass(message, Rs3Transcriber::eventMouseMove)
            name == "EVENT_NATIVE_MOUSE_MOVE" -> pass(message, Rs3Transcriber::eventNativeMouseMove)
            name == "SEND_SNAPSHOT" -> pass(message, Rs3Transcriber::sendSnapshot)
            name == "ADD_NEW_EMAIL_ADDRESS" -> pass(message, Rs3Transcriber::addNewEmailAddress)
            name == "CHANGE_EMAIL_ADDRESS" -> pass(message, Rs3Transcriber::changeEmailAddress)
            name == "UNNAMED_LOBBY_REQUEST" -> pass(message, Rs3Transcriber::unnamedLobbyRequest)
            name == "MESSAGE_QUICKCHAT_PUBLIC" -> pass(message, Rs3Transcriber::messageQuickchatPublic)
            name == "MESSAGE_QUICKCHAT_PRIVATE" -> pass(message, Rs3Transcriber::messageQuickchatPrivate)
            name == "STORE_SERVERPERM_VARCS" -> pass(message, Rs3Transcriber::storeServerPermVarcs)
            name == "CLIENT_PREFERENCES" -> pass(message, Rs3Transcriber::clientPreferences)
            else -> Unit
        }
    }

    override fun onServerPacket(prot: ClientProt, message: IncomingMessage) {
        if (message is Reconnect) {
            pass(message, Rs3Transcriber::reconnect)
            return
        }
        if (message is NoTimeout) {
            pass<NoTimeout>(message, Rs3Transcriber::noTimeout)
            return
        }
        if (message is RebuildNormal) {
            pass(message, Rs3Transcriber::rebuildNormal)
            return
        }
        if (message is CameraUpdate) {
            pass(message, Rs3Transcriber::cameraUpdate)
            return
        }
        if (message is HintTrail) {
            pass(message, Rs3Transcriber::hintTrail)
            return
        }
        if (message is HintArrow) {
            pass(message, Rs3Transcriber::hintArrow)
            return
        }
        if (message is ProjAnimSpecificV2) {
            pass(message, Rs3Transcriber::projAnimSpecificV2)
            return
        }
        if (message is SetPlayerOp) {
            pass(message, Rs3Transcriber::setPlayerOp)
            return
        }
        if (message is MessageGame) {
            pass(message, Rs3Transcriber::messageGame)
            return
        }
        if (message is CamForceAngle) {
            pass(message, Rs3Transcriber::camForceAngle)
            return
        }
        if (message is CamShake) {
            pass(message, Rs3Transcriber::camShake)
            return
        }
        if (message is MidiSongLocation) {
            pass(message, Rs3Transcriber::midiSongLocation)
            return
        }
        if (message is MapProjAnimHalfsqV2) {
            pass(message, Rs3Transcriber::mapProjAnimHalfsqV2)
            return
        }
        if (message is MapProjAnimHalfsq) {
            pass(message, Rs3Transcriber::mapProjAnimHalfsq)
            return
        }
        if (message is MapProjAnimV2) {
            pass(message, Rs3Transcriber::mapProjAnimV2)
            return
        }
        if (message is MapProjAnim) {
            pass(message, Rs3Transcriber::mapProjAnim)
            return
        }
        if (message is SoundAreaV2) {
            pass(message, Rs3Transcriber::soundAreaV2)
            return
        }
        if (message is SoundAreaV1) {
            pass(message, Rs3Transcriber::soundAreaV1)
            return
        }
        if (message is LocCustomise) {
            pass(message, Rs3Transcriber::locCustomise)
            return
        }
        if (message is UnknownServerPacket) {
            pass(message, Rs3Transcriber::unknownServerOpcode)
            return
        }
        when (prot.toString()) {
            "LOGOUT" -> pass(message, Rs3Transcriber::logout)
            "FRIENDLIST_LOADED" -> pass(message, Rs3Transcriber::friendlistLoaded)
            "VARCLAN_ENABLE" -> pass(message, Rs3Transcriber::varclanEnable)
            "VARCLAN_DISABLE" -> pass(message, Rs3Transcriber::varclanDisable)
            "RESET_ANIMS" -> pass(message, Rs3Transcriber::resetAnims)
            "LOCSELECT_CLEAR" -> pass(message, Rs3Transcriber::locSelectClear)
            "STORE_SERVERPERM_VARCS_ACK" -> pass(message, Rs3Transcriber::storeServerpermVarcsAck)
            "JS5_RELOAD" -> pass(message, Rs3Transcriber::js5Reload)
            "STORE_RESET" -> pass(message, Rs3Transcriber::storeReset)
            "CREATE_CHECK_NAME_REPLY" -> pass(message, Rs3Transcriber::createCheckNameReply)
            "CREATE_CHECK_EMAIL_REPLY" -> pass(message, Rs3Transcriber::createCheckEmailReply)
            "CREATE_ACCOUNT_REPLY" -> pass(message, Rs3Transcriber::createAccountReply)
            "CREATE_SUGGEST_NAME_ERROR" -> pass(message, Rs3Transcriber::createSuggestNameError)
            "UPDATE_DOB" -> pass(message, Rs3Transcriber::updateDob)
            "EXECUTE_CLIENT_CHEAT" -> pass(message, Rs3Transcriber::executeClientCheat)
            "MESSAGE_QUICKCHAT_PRIVATE" ->
                pass<MessageQuickchatPrivate>(message, Rs3Transcriber::messageQuickchatPrivateServer)
            "MESSAGE_QUICKCHAT_PRIVATE_ECHO" ->
                pass<MessageQuickchatPrivateEcho>(message, Rs3Transcriber::messageQuickchatPrivateEcho)
            "MESSAGE_QUICKCHAT_CLANCHANNEL" ->
                pass<MessageQuickchatClanchannel>(message, Rs3Transcriber::messageQuickchatClanchannel)
            "MESSAGE_QUICKCHAT_FRIENDCHAT" ->
                pass<MessageQuickchatFriendchat>(message, Rs3Transcriber::messageQuickchatFriendchat)
            "MESSAGE_QUICKCHAT_PLAYER_GROUP" ->
                pass<MessageQuickchatPlayerGroup>(message, Rs3Transcriber::messageQuickchatPlayerGroup)
            "VARCLAN" -> pass<Varclan>(message, Rs3Transcriber::varclan)
            "LOBBY_APPEARANCE" -> pass<LobbyAppearance>(message, Rs3Transcriber::lobbyAppearance)
            "PLAYER_SNAPSHOT" -> pass<PlayerSnapshot>(message, Rs3Transcriber::playerSnapshot)
            "CLEAR_PLAYER_SNAPSHOT" -> pass(message, Rs3Transcriber::clearPlayerSnapshot)
            "TELEMETRY_GRID_ADD_COLUMN" -> pass(message, Rs3Transcriber::telemetryGridAddColumn)
            "TELEMETRY_GRID_REMOVE_ROW" -> pass(message, Rs3Transcriber::telemetryGridRemoveRow)
            "TELEMETRY_GRID_SET_ROW_PINNED" -> pass(message, Rs3Transcriber::telemetryGridSetRowPinned)
            "TELEMETRY_GRID_MOVE_COLUMN" -> pass(message, Rs3Transcriber::telemetryGridMoveColumn)
            "TELEMETRY_GRID_ADD_GROUP" -> pass(message, Rs3Transcriber::telemetryGridAddGroup)
            "TELEMETRY_GRID_MOVE_ROW" -> pass(message, Rs3Transcriber::telemetryGridMoveRow)
            "TELEMETRY_GRID_ADD_ROW" -> pass(message, Rs3Transcriber::telemetryGridAddRow)
            "TELEMETRY_CLEAR_GRID_VALUE" -> pass(message, Rs3Transcriber::telemetryClearGridValue)
            "TELEMETRY_GRID_REMOVE_GROUP" -> pass(message, Rs3Transcriber::telemetryGridRemoveGroup)
            "TELEMETRY_GRID_REMOVE_COLUMN" -> pass(message, Rs3Transcriber::telemetryGridRemoveColumn)
            "SEND_PING" -> pass(message, Rs3Transcriber::sendPing)
            "UPDATE_UID192" -> pass(message, Rs3Transcriber::updateUid192)
            "SET_MAP_FLAG" -> pass(message, Rs3Transcriber::setMapFlag)
            "LOCSELECT_CONFIGURE" -> pass(message, Rs3Transcriber::locSelectConfigure)
            "LOC_ANIM_SPECIFIC" -> pass(message, Rs3Transcriber::locAnimSpecific)
            "NPC_ANIM_SPECIFIC" -> pass(message, Rs3Transcriber::npcAnimSpecific)
            "PLAYER_ANIM_SPECIFIC" -> pass(message, Rs3Transcriber::playerAnimSpecific)
            "NPC_HEADICON_SPECIFIC" -> pass(message, Rs3Transcriber::npcHeadiconSpecific)
            "SPOTANIM_SPECIFIC" -> pass(message, Rs3Transcriber::spotanimSpecific)
            "SPOTANIM_SPECIFIC_V2" -> pass(message, Rs3Transcriber::spotanimSpecificV2)
            "SET_MOVEACTION" -> pass(message, Rs3Transcriber::setMoveAction)
            "CREATE_SUGGEST_NAME_REPLY" -> pass(message, Rs3Transcriber::createSuggestNameReply)
            "NPC_SAY_SPECIFIC" -> pass(message, Rs3Transcriber::npcSaySpecific)
            "DO_CHEAT" -> pass(message, Rs3Transcriber::doCheat)
            "LOGOUT_TRANSFER" -> pass(message, Rs3Transcriber::logoutTransfer)
            "CHANGE_LOBBY" -> pass(message, Rs3Transcriber::changeLobby)
            "SET_LOC_OP_OVERRIDE" -> pass(message, Rs3Transcriber::setLocOpOverride)
            "PROJANIM_SPECIFIC" -> pass(message, Rs3Transcriber::projAnimSpecific)
            "DEBUG_SERVER_TRIGGERS" -> pass(message, Rs3Transcriber::debugServerTriggers)
            "UNNAMED_1" -> pass(message, Rs3Transcriber::unnamed1)
            "UNNAMED_2" -> pass(message, Rs3Transcriber::unnamed2)
            "UPDATE_FRIENDLIST" -> pass(message, Rs3Transcriber::updateFriendlist)
            "UPDATE_IGNORELIST" -> pass(message, Rs3Transcriber::updateIgnorelist)
            "UPDATE_FRIENDCHAT_CHANNEL_FULL" -> pass(message, Rs3Transcriber::updateFriendchatChannelFull)
            "UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER" -> pass(message, Rs3Transcriber::updateFriendchatChannelSingleUser)
            "TELEMETRY_GRID_VALUES_DELTA" -> pass(message, Rs3Transcriber::telemetryGridValuesDelta)
            "TELEMETRY_GRID_FULL" -> pass(message, Rs3Transcriber::telemetryGridFull)
            "LOCSELECT_ADD" -> pass(message, Rs3Transcriber::locSelectAdd)
            "REBUILD_REGION" -> pass(message, Rs3Transcriber::rebuildRegion)
            "CONSOLE_FEEDBACK" -> pass(message, Rs3Transcriber::consoleFeedback)
            "UPDATE_STOCKMARKET_SLOT_V2" -> pass(message, Rs3Transcriber::updateStockmarketSlotV2)
            "ENVIRONMENT_OVERRIDE" -> pass(message, Rs3Transcriber::environmentOverride)
            "CLANCHANNEL_FULL" -> pass(message, Rs3Transcriber::clanChannelFull)
            "CLANCHANNEL_DELTA" -> pass(message, Rs3Transcriber::clanChannelDelta)
            "CLANSETTINGS_FULL" -> pass(message, Rs3Transcriber::clanSettingsFull)
            "CLANSETTINGS_DELTA" -> pass(message, Rs3Transcriber::clanSettingsDelta)
            "LAST_LOGIN_INFO" -> pass(message, Rs3Transcriber::lastLoginInfo)
            "UPDATE_REBOOT_TIMER" -> pass(message, Rs3Transcriber::updateRebootTimer)
            "CHAT_FILTER_SETTINGS" -> pass(message, Rs3Transcriber::chatFilterSettings)
            "LOGOUT_FULL" -> pass(message, Rs3Transcriber::logoutFull)
            "CAM_RESET" -> pass(message, Rs3Transcriber::camReset)
            "CAM2_ENABLE" -> pass(message, Rs3Transcriber::cam2Enable)
            "CAM_SMOOTHRESET" -> pass(message, Rs3Transcriber::camSmoothReset)
            "SHOW_FACE_HERE" -> pass(message, Rs3Transcriber::showFaceHere)
            "SETDRAWORDER" -> pass(message, Rs3Transcriber::setDrawOrder)
            "TRIGGER_ONDIALOGABORT" -> pass(message, Rs3Transcriber::triggerOnDialogAbort)
            "RESET_CLIENT_VARCACHE" -> pass(message, Rs3Transcriber::resetClientVarcache)
            "IF_OPENSUB_ACTIVE_NPC" -> pass(message, Rs3Transcriber::ifOpenSubActiveNpc)
            "IF_OPENSUB_ACTIVE_PLAYER" -> pass(message, Rs3Transcriber::ifOpenSubActivePlayer)
            "VARBIT" -> pass(message, Rs3Transcriber::varbit)
            "CLIENT_SETVARCBIT" -> pass(message, Rs3Transcriber::varcBit)
            "CLIENT_SETVARC_LONG" -> pass(message, Rs3Transcriber::varcLong)
            "CLIENT_SETVARCSTR_LARGE" -> pass(message, Rs3Transcriber::varcStrLarge)
            "VORBIS_SPEECH_STOP" -> pass(message, Rs3Transcriber::vorbisSpeechStop)
            "MIDI_SONG_STOP" -> pass(message, Rs3Transcriber::midiSongStop)
            "MIDI_JINGLE" -> pass(message, Rs3Transcriber::midiJingle)
            "SOUND_STOP" -> pass(message, Rs3Transcriber::soundStop)
            "SOUND_MIXBUSS_SETLEVEL" -> pass(message, Rs3Transcriber::soundMixbussSetLevel)
            "MIDI_SONG" -> pass(message, Rs3Transcriber::midiSong)
            "SONG_PRELOAD" -> pass(message, Rs3Transcriber::songPreload)
            "VORBIS_SOUND_GROUP_STOP" -> pass(message, Rs3Transcriber::vorbisSoundGroupStop)
            "VORBIS_SOUND_GROUP_START" -> pass(message, Rs3Transcriber::vorbisSoundGroupStart)
            "SOUND_MIXBUSS_ADD" -> pass(message, Rs3Transcriber::soundMixbussAdd)
            "VORBIS_PRELOAD_SOUNDS" -> pass(message, Rs3Transcriber::vorbisPreloadSounds)
            "VORBIS_SOUND_GROUP" -> pass(message, Rs3Transcriber::vorbisSoundGroup)
            "VORBIS_SPEECH_SOUND" -> pass(message, Rs3Transcriber::vorbisSpeechSound)
            "SYNTH_SOUND" -> pass(message, Rs3Transcriber::synthSound)
            "VARP_SMALL" -> pass(message, Rs3Transcriber::varpSmall)
            "VARP_LARGE" -> pass(message, Rs3Transcriber::varpLarge)
            "VARP_LONG" -> pass(message, Rs3Transcriber::varpLong)
            "VARBIT_SMALL" -> pass(message, Rs3Transcriber::varbitSmall)
            "VARBIT_LARGE" -> pass(message, Rs3Transcriber::varbitLarge)
            "IF_SETPLAYERHEAD_IGNOREWORN" -> pass(message, Rs3Transcriber::ifSetPlayerHeadIgnoreWorn)
            "IF_SETPLAYERHEAD_OTHER" -> pass(message, Rs3Transcriber::ifSetPlayerHeadOther)
            "IF_SETPLAYERMODEL_OTHER" -> pass(message, Rs3Transcriber::ifSetPlayerModelOther)
            "IF_SETANGLE" -> pass(message, Rs3Transcriber::ifSetAngle)
            "IF_SETTEXTANTIMACRO" -> pass(message, Rs3Transcriber::ifSetTextAntiMacro)
            "IF_SETCLICKMASK" -> pass(message, Rs3Transcriber::ifSetClickMask)
            "IF_SETTEXTFONT" -> pass(message, Rs3Transcriber::ifSetTextFont)
            "IF_SETGRAPHIC" -> pass(message, Rs3Transcriber::ifSetGraphic)
            "IF_SETRECOL" -> pass(message, Rs3Transcriber::ifSetRecol)
            "IF_SETRETEX" -> pass(message, Rs3Transcriber::ifSetRetex)
            "IF_MOVESUB" -> pass(message, Rs3Transcriber::ifMoveSub)
            "IF_SET_HTTP_IMAGE" -> pass(message, Rs3Transcriber::ifSetHttpImage)
            "IF_SETOBJECT_LONG_V2" -> pass(message, Rs3Transcriber::ifSetObjectLongV2)
            "SET_NPC_ATTACK_PRIORITY" -> pass(message, Rs3Transcriber::setNpcAttackPriority)
            "SET_PLAYER_ATTACK_PRIORITY" -> pass(message, Rs3Transcriber::setPlayerAttackPriority)
            "SET_TARGET" -> pass(message, Rs3Transcriber::setTarget)
            "LOYALTY_UPDATE" -> pass(message, Rs3Transcriber::loyaltyUpdate)
            "SYNC_CLOCK" -> pass(message, Rs3Transcriber::syncClock)
            "CAM_REMOVEROOF" -> pass(message, Rs3Transcriber::camRemoveRoof)
            "POINTLIGHT_EXTEND_ABOVE" -> pass(message, Rs3Transcriber::pointLightExtendAbove)
            "POINTLIGHT_EXTEND_BELOW" -> pass(message, Rs3Transcriber::pointLightExtendBelow)
            "POINTLIGHT_ATTENUATION_FALLOFF" -> pass(message, Rs3Transcriber::pointLightAttenuationFalloff)
            "POINTLIGHT_INTENSITYSCALE" -> pass(message, Rs3Transcriber::pointLightIntensityScale)
            "POINTLIGHT_COLOUR" -> pass(message, Rs3Transcriber::pointLightColour)
            "POINTLIGHT_ENABLED" -> pass(message, Rs3Transcriber::pointLightEnabled)
            "POINTLIGHT_SHADOW" -> pass(message, Rs3Transcriber::pointLightShadow)
            "IF_OPENTOP" -> pass(message, Rs3Transcriber::ifOpenTop)
            "IF_OPENSUB" -> pass(message, Rs3Transcriber::ifOpenSub)
            "IF_CLOSESUB" -> pass(message, Rs3Transcriber::ifCloseSub)
            "IF_SETHIDE" -> pass(message, Rs3Transcriber::ifSetHide)
            "MESSAGE_PUBLIC" -> pass<MessagePublic>(message, Rs3Transcriber::messagePublic)
            "PLAYER_GROUP_FULL" -> pass(message, Rs3Transcriber::playerGroupFull)
            "PLAYER_GROUP_DELTA" -> pass(message, Rs3Transcriber::playerGroupDelta)
            "PLAYER_GROUP_VARPS" -> pass(message, Rs3Transcriber::playerGroupVarps)
            "MESSAGE_PRIVATE_ECHO" -> pass(message, Rs3Transcriber::messagePrivateEcho)
            "MESSAGE_PRIVATE" -> pass<MessagePrivate>(message, Rs3Transcriber::messagePrivate)
            "MESSAGE_CLANCHANNEL" -> pass(message, Rs3Transcriber::messageClanchannel)
            "MESSAGE_FRIENDCHANNEL" -> pass(message, Rs3Transcriber::messageFriendchannel)
            "MESSAGE_PLAYER_GROUP" -> pass(message, Rs3Transcriber::messagePlayerGroup)
            "MESSAGE_CLANCHANNEL_SYSTEM" -> pass(message, Rs3Transcriber::messageClanchannelSystem)
            "DBFILTER_DEBUG" -> pass(message, Rs3Transcriber::dbFilterDebug)
            "URL_OPEN" -> pass(message, Rs3Transcriber::urlOpen)
            "SOCIAL_NETWORK_LOGOUT" -> pass(message, Rs3Transcriber::socialNetworkLogout)
            "UPDATE_SITESETTINGS" -> pass(message, Rs3Transcriber::siteSettings)
            "WORLDLIST_FETCH_REPLY" -> pass(message, Rs3Transcriber::worldlistFetchReply)
            "NPC_INFO_V2" -> pass(message, Rs3Transcriber::npcInfo)
            "PLAYER_INFO" -> pass(message, Rs3Transcriber::playerInfo)
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
            "MAP_ANIM_V1" -> pass(message, Rs3Transcriber::mapAnimV1)
            "MAP_ANIM_V2" -> pass(message, Rs3Transcriber::mapAnimV2)
            "TEXT_COORD" -> pass(message, Rs3Transcriber::textCoord)
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
            "CAM_MOVETO" -> pass(message, Rs3Transcriber::camMoveTo)
            "UPDATE_INV_FULL" -> pass(message, Rs3Transcriber::updateInvFull)
            "UPDATE_INV_STOP_TRANSMIT" -> pass(message, Rs3Transcriber::updateInvStopTransmit)
            "UPDATE_INV_PARTIAL" -> pass(message, Rs3Transcriber::updateInvPartial)
            "UPDATE_RUNWEIGHT" -> pass(message, Rs3Transcriber::updateRunWeight)
            "UPDATE_STAT" -> pass(message, Rs3Transcriber::updateStat)
            "UPDATE_RUNENERGY" -> pass(message, Rs3Transcriber::updateRunEnergy)
            "MINIMAP_TOGGLE" -> pass(message, Rs3Transcriber::minimapToggle)
            "CHAT_FILTER_SETTINGS_PRIVATECHAT" -> pass(message, Rs3Transcriber::chatFilterSettingsPrivateChat)
            "IF_SETPLAYERMODEL_SNAPSHOT" -> pass(message, Rs3Transcriber::ifSetPlayerModelSnapshot)
            "IF_SETPLAYERHEAD_SNAPSHOT" -> pass(message, Rs3Transcriber::ifSetPlayerHeadSnapshot)
            "VORBIS_SOUND" -> pass(message, Rs3Transcriber::vorbisSound)
            "RUNCLIENTSCRIPT" -> pass(message, Rs3Transcriber::runClientScript)
            "LOC_PREFETCH" -> pass(message, Rs3Transcriber::locPrefetch)
            "CUTSCENE2D_PLAY" -> pass(message, Rs3Transcriber::cutscene2dPlay)
            "JCOINS_UPDATE" -> pass(message, Rs3Transcriber::jcoinsUpdate)
            "SERVER_TICK_END" -> pass(message, Rs3Transcriber::tickEnd)
            "NO_TIMEOUT" -> Unit
            else -> Unit
        }
    }
}
