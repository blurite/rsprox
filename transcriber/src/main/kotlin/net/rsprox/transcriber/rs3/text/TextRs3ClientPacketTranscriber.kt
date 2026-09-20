package net.rsprox.transcriber.rs3.text

import net.rsprox.cache.api.rs3.Rs3VariableDomain
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.incoming.model.unknown.UnknownClientPacket
import net.rsprox.protocol.rs3.game.incoming.model.account.AddNewEmailAddress
import net.rsprox.protocol.rs3.game.incoming.model.account.ChangeEmailAddress
import net.rsprox.protocol.rs3.game.incoming.model.account.CreateLogProgress
import net.rsprox.protocol.rs3.game.incoming.model.account.CreateSuggestNames
import net.rsprox.protocol.rs3.game.incoming.model.account.SendEmailValidationCode
import net.rsprox.protocol.rs3.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfCrmViewOp
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfPlayer
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfTextChange
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfValueChange32
import net.rsprox.protocol.rs3.game.incoming.model.chat.ChatSetMode
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePrivate
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessagePublic
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessageQuickchatPrivate
import net.rsprox.protocol.rs3.game.incoming.model.chat.MessageQuickchatPublic
import net.rsprox.protocol.rs3.game.incoming.model.chat.SetChatFilterSettings
import net.rsprox.protocol.rs3.game.incoming.model.dialog.AbortPDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePClanForumQfcDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePCountDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePHslDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePLongDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePNameDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePObjDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePStringDialog
import net.rsprox.protocol.rs3.game.incoming.model.dialog.ResumePauseButton
import net.rsprox.protocol.rs3.game.incoming.model.events.ClientDetailOptionsStatus
import net.rsprox.protocol.rs3.game.incoming.model.events.Cutscene2DFinished
import net.rsprox.protocol.rs3.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.rs3.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.rs3.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.rs3.game.incoming.model.events.EventMouseClick
import net.rsprox.protocol.rs3.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.rs3.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.rs3.game.incoming.model.events.EventNativeMouseMove
import net.rsprox.protocol.rs3.game.incoming.model.events.MidiSongStop
import net.rsprox.protocol.rs3.game.incoming.model.events.MouseMovement
import net.rsprox.protocol.rs3.game.incoming.model.events.PingStatistics
import net.rsprox.protocol.rs3.game.incoming.model.events.SendPingReply
import net.rsprox.protocol.rs3.game.incoming.model.events.TransmitVarVerifyId
import net.rsprox.protocol.rs3.game.incoming.model.events.WindowStatus
import net.rsprox.protocol.rs3.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.rs3.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.StoreServerPermVarcs
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.UidPassportResendRequest
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ApCoordT
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.FaceSquare
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.LocSelectSubmit
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.MoveScripted
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.UrlRequest
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.WorldListFetch
import net.rsprox.protocol.rs3.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.rs3.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.rs3.game.incoming.model.objs.OpObj
import net.rsprox.protocol.rs3.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.rs3.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.rs3.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.rs3.game.incoming.model.social.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.rs3.game.incoming.model.social.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.rs3.game.incoming.model.social.ClanChannelKickUser
import net.rsprox.protocol.rs3.game.incoming.model.social.ClanJoinChatLeaveChat
import net.rsprox.protocol.rs3.game.incoming.model.social.ClanKickUser
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendSetNotes
import net.rsprox.protocol.rs3.game.incoming.model.social.FriendSetRank
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreListDel
import net.rsprox.protocol.rs3.game.incoming.model.social.IgnoreSetNotes
import net.rsprox.protocol.rs3.game.incoming.model.unknown.RawUnknownClientPacket
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.any
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.filteredScriptVarType
import net.rsprox.shared.property.group
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.long
import net.rsprox.shared.property.regular.AnyProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.shortNpc
import net.rsprox.shared.property.shortPlayer
import net.rsprox.shared.property.string
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.rs3.interfaces.Rs3ClientPacketTranscriber
import net.rsprox.transcriber.rs3.state.Rs3SessionState

public class TextRs3ClientPacketTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3ClientPacketTranscriber {
    private val coordinates = Rs3CoordinateProperties(sessionState, settingSetStore)

    private fun Property.coordGrid(
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> = coordinates.append(this, level, x, z, name)

    private val root: RootProperty
        get() =
            checkNotNull(sessionState.root.lastOrNull()) {
                "No active root - onTranscribeStart() must run before dispatching to a transcriber method"
            }
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()
    private val settings: SettingSet
        get() = settingSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    private fun Property.npc(index: Int): ChildProperty<*> {
        val npc = sessionState.getActiveWorld().getNpcOrNull(index) ?: return unidentifiedNpc(index)
        // NPC identity is tracked, but absolute NPC coordinates are not yet available.
        return shortNpc(index, npc.id)
    }

    private fun Property.player(index: Int): ChildProperty<*> {
        val player = sessionState.getPlayerOrNull(index) ?: return unidentifiedPlayer(index)
        if (player.name == null || player.level == null || player.x == null || player.z == null) {
            return shortPlayer(index, player.name)
        }
        val finalIndex =
            if (settings[Setting.PLAYER_HIDE_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        val coord = coordinates.translate(CoordGrid(player.level, player.x, player.z))
        return identifiedPlayer(finalIndex, player.name, coord.level, coord.x, coord.z)
    }

    override fun if3Button(message: If3Button) {
        if (!filters[PropertyFilter.IF_BUTTON]) return omit()
        root.component("com", message.combinedId)
        root.filteredInt("sub", message.slot, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.obj, -1)
        // The operation is encoded by the individual packet identity, unlike OSRS IF_BUTTONX.
    }

    override fun opNpc(message: OpNpc) {
        if (!filters[PropertyFilter.OPNPC]) return omit()
        root.npc(message.index)
        root.filteredBoolean("ctrl", message.run)
    }

    override fun opLoc(message: OpLoc) {
        if (!filters[PropertyFilter.OPLOC]) return omit()
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
        root.coordGrid(sessionState.level(), message.x, message.y)
        root.filteredBoolean("ctrl", message.run)
    }

    override fun opObj(message: OpObj) {
        if (!filters[PropertyFilter.OPOBJ]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(sessionState.level(), message.x, message.y)
        root.filteredBoolean("ctrl", message.run)
        message.flags?.let { root.int("flags", it) }
    }

    override fun opPlayer(message: OpPlayer) {
        if (!filters[PropertyFilter.OPPLAYER]) return omit()
        root.player(message.index)
        root.filteredBoolean("ctrl", message.run)
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
        if (!filters[PropertyFilter.EVENT_APPLET_FOCUS]) return omit()
        root.boolean("infocus", message.inFocus)
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
        if (!filters[PropertyFilter.EVENT_NATIVE_MOUSE_CLICK]) return omit()
        root.int("lasttransmitted", message.lastTransmittedMouseClick)
        root.int("x", message.x)
        root.int("y", message.y)
        root.int("code", message.code)
    }

    override fun moveGameClick(message: MoveGameClick) {
        if (!filters[PropertyFilter.MOVE_GAMECLICK]) return omit()
        root.movementCoordinate(message.x, message.y)
        root.filteredBoolean("ctrl", message.run)
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
        if (!filters[PropertyFilter.MOVE_MINIMAPCLICK]) return omit()
        root.movementCoordinate(message.x, message.z)
        root.filteredInt("ctrl", message.controlKey, 0)
        root.int("finex", message.playerX)
        root.int("finez", message.playerZ)
        root.children += AnyProperty("reserved", message.reservedMinimapMetadata, List::class.java)
        root.int("sentinel", message.sentinel)
    }

    override fun moveScripted(message: MoveScripted) {
        if (!filters[PropertyFilter.MOVE_SCRIPTED]) return omit()
        root.movementCoordinate(message.x, message.z)
        root.int("mode", message.mode)
    }

    private fun Property.movementCoordinate(
        x: Int,
        z: Int,
    ) {
        val level = sessionState.getPlayerOrNull(sessionState.localPlayerIndex)?.level
        if (level != null) {
            coordGrid(sessionState.level(), x, z)
            return
        }
        // Movement supplies X/Z only. Preserve them even before player-info has established a level.
        int("x", x)
        int("z", z)
        any("level", "untracked")
    }

    override fun opNpcT(message: OpNpcT) {
        if (!filters[PropertyFilter.OPNPCT]) return omit()
        root.npc(message.index)
        root.filteredInt("ctrl", message.controlKey, 0)
        root.component("com", message.selectedCombinedId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opPlayerT(message: OpPlayerT) {
        if (!filters[PropertyFilter.OPPLAYERT]) return omit()
        root.player(message.index)
        root.filteredInt("ctrl", message.controlKey, 0)
        root.component("com", message.selectedCombinedId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opLocT(message: OpLocT) {
        if (!filters[PropertyFilter.OPLOCT]) return omit()
        root.scriptVarType("id", ScriptVarType.LOC, message.id)
        root.coordGrid(sessionState.level(), message.x, message.z)
        root.filteredInt("ctrl", message.controlKey, 0)
        root.component("com", message.selectedCombinedId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun opObjT(message: OpObjT) {
        if (!filters[PropertyFilter.OPOBJT]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.id)
        root.coordGrid(sessionState.level(), message.x, message.z)
        root.int("flags", message.flags)
        root.component("selectedcom", message.selectedCombinedId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun apCoordT(message: ApCoordT) {
        if (!filters[PropertyFilter.APCOORDT]) return omit()
        root.coordGrid(sessionState.level(), message.x, message.z)
        root.component("com", message.selectedCombinedId)
        root.filteredInt("sub", message.selectedSub, -1)
        root.filteredScriptVarType("obj", ScriptVarType.OBJ, message.selectedObj, -1)
    }

    override fun ifButtonT(message: IfButtonT) {
        if (!filters[PropertyFilter.IF_BUTTONT]) return omit()
        root.component("selectcom", message.selectedCombinedId)
        root.filteredInt("selectedsub", message.selectedSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.selectedObj, -1)
        root.component("targetcom", message.targetCombinedId)
        root.filteredInt("targetsub", message.targetSub, -1)
        root.filteredScriptVarType("targetobj", ScriptVarType.OBJ, message.targetObj, -1)
    }

    override fun ifButtonD(message: IfButtonD) {
        if (!filters[PropertyFilter.IF_BUTTOND]) return omit()
        root.component("selectcom", message.sourceCombinedId)
        root.filteredInt("selectedsub", message.sourceSub, -1)
        root.filteredScriptVarType("selectedobj", ScriptVarType.OBJ, message.sourceObj, -1)
        root.component("targetcom", message.targetCombinedId)
        root.filteredInt("targetsub", message.targetSub, -1)
        root.filteredScriptVarType("targetobj", ScriptVarType.OBJ, message.targetObj, -1)
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
        if (!filters[PropertyFilter.EVENT_CAMERA_POSITION]) return omit()
        root.int("yaw", message.yaw)
        root.int("pitch", message.pitch)
    }

    override fun windowStatus(message: WindowStatus) {
        if (!filters[PropertyFilter.WINDOW_STATUS]) return omit()
        root.int("windowmode", message.mode)
        root.int("framewidth", message.width)
        root.int("frameheight", message.height)
        root.int("antialias", message.antialias)
    }

    override fun resumePauseButton(message: ResumePauseButton) {
        if (!filters[PropertyFilter.RESUME_PAUSEBUTTON]) return omit()
        root.component("com", message.combinedId)
        root.int("sub", if (message.sub == 65535) -1 else message.sub)
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
        if (!filters[PropertyFilter.RESUME_P_COUNTDIALOG]) return omit()
        root.int("count", message.value)
    }

    override fun resumePLongDialog(message: ResumePLongDialog) {
        if (!filters[PropertyFilter.RESUME_P_LONGDIALOG]) return omit()
        root.long("count", message.value)
    }

    override fun resumePHslDialog(message: ResumePHslDialog) {
        if (!filters[PropertyFilter.RESUME_P_HSLDIALOG]) return omit()
        root.int("hsl", message.hsl)
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
        if (!filters[PropertyFilter.RESUME_P_OBJDIALOG]) return omit()
        root.scriptVarType("id", ScriptVarType.OBJ, message.obj)
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
        if (!filters[PropertyFilter.RESUME_P_STRINGDIALOG]) return omit()
        root.string("string", message.value)
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
        if (!filters[PropertyFilter.RESUME_P_NAMEDIALOG]) return omit()
        root.string("name", message.value)
    }

    override fun resumePClanForumQfcDialog(message: ResumePClanForumQfcDialog) {
        if (!filters[PropertyFilter.RESUME_P_CLANFORUMQFCDIALOG]) return omit()
        root.string("value", message.value)
    }

    override fun messagePublic(message: MessagePublic) {
        if (!filters[PropertyFilter.MESSAGE_PUBLIC]) return omit()
        root.children += AnyProperty("colourandeffect", message.colourAndEffect, List::class.java)
        root.string("message", message.message)
    }

    override fun messagePrivate(message: MessagePrivate) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE_CLIENT]) return omit()
        root.string("to", message.recipient)
        root.string("message", message.message)
    }

    override fun clientCheat(message: ClientCheat) {
        if (!filters[PropertyFilter.CLIENT_CHEAT]) return omit()
        root.int("flaga", message.flagA)
        root.int("flagb", message.flagB)
        root.string("command", message.command)
    }

    override fun chatSetMode(message: ChatSetMode) {
        if (!filters[PropertyFilter.CHAT_SETMODE]) return omit()
        root.int("channel", message.channel)
        root.int("mode", message.mode)
    }

    override fun setChatFilterSettings(message: SetChatFilterSettings) {
        if (!filters[PropertyFilter.SET_CHATFILTERSETTINGS]) return omit()
        root.int("public", message.publicMode)
        root.int("private", message.privateMode)
        root.int("trade", message.tradeMode)
    }

    override fun closeModal(message: CloseModal) {
        if (!filters[PropertyFilter.CLOSE_MODAL]) return omit()
    }

    override fun abortPDialog(message: AbortPDialog) {
        if (!filters[PropertyFilter.ABORT_P_DIALOG]) return omit()
    }

    override fun createSuggestNames(message: CreateSuggestNames) {
        if (!filters[PropertyFilter.CREATE_SUGGEST_NAMES]) return omit()
    }

    override fun noTimeout(message: NoTimeout) {
        if (!filters[PropertyFilter.NO_TIMEOUT]) return omit()
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
        if (!filters[PropertyFilter.MAP_BUILD_COMPLETE]) return omit()
        root.int("builddurationmillis", message.buildDurationMillis)
    }

    override fun ifPlayer(message: IfPlayer) {
        if (!filters[PropertyFilter.IF_PLAYER]) return omit()
        root.string("crmname", message.crmName)
        root.component("com", message.combinedId)
        root.int("operation", message.operation)
        root.int("sub", message.sub)
        root.int("crmtype", message.crmType)
    }

    override fun ignoreListDel(message: IgnoreListDel) {
        if (!filters[PropertyFilter.IGNORELIST_DEL]) return omit()
        root.string("name", message.name)
    }

    override fun friendListAdd(message: FriendListAdd) {
        if (!filters[PropertyFilter.FRIENDLIST_ADD]) return omit()
        root.string("name", message.name)
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
        if (!filters[PropertyFilter.CLANCHANNEL_KICKUSER]) return omit()
        root.string("name", message.name)
        root.int("clanid", message.channel)
        root.int("memberindex", message.member)
    }

    override fun clickWorldMap(message: ClickWorldMap) {
        if (!filters[PropertyFilter.CLICKWORLDMAP]) return omit()
        root.scriptVarType("coord", ScriptVarType.COORDGRID, message.packedCoordinate)
    }

    override fun bugReport(message: BugReport) {
        if (!filters[PropertyFilter.BUG_REPORT]) return omit()
        root.int("reportcategory", message.reportCategory)
        root.string("details", message.details)
        root.string("summary", message.summary)
    }

    override fun ifCrmViewOp(message: IfCrmViewOp) {
        if (!filters[PropertyFilter.IF_CRMVIEW_OP]) return omit()
        root.int("sub", message.sub)
        root.int("crmvalue0", message.crmValue0)
        root.int("crmvalue2", message.crmValue2)
        root.int("crmvalue1", message.crmValue1)
        root.component("com", message.combinedId)
        root.int("selectedcrmentry", message.selectedCrmEntry)
    }

    override fun cutscene2DFinished(message: Cutscene2DFinished) {
        if (!filters[PropertyFilter.CUTSCENE2D_FINISHED]) return omit()
        root.int("status", message.status)
        root.int("id", message.id)
    }

    override fun transmitVarVerifyId(message: TransmitVarVerifyId) {
        if (!filters[PropertyFilter.TRANSMITVAR_VERIFYID]) return omit()
        root.int("verifyid", message.verifyId)
    }

    override fun pingStatistics(message: PingStatistics) {
        if (!filters[PropertyFilter.PING_STATISTICS]) return omit()
        root.int("latency", message.latency)
        root.int("reserved", message.reserved)
        root.int("fps", message.fps)
    }

    override fun sendEmailValidationCode(message: SendEmailValidationCode) {
        if (!filters[PropertyFilter.SEND_EMAIL_VALIDATION_CODE]) return omit()
        root.string("code", message.code)
    }

    override fun friendListDel(message: FriendListDel) {
        if (!filters[PropertyFilter.FRIENDLIST_DEL]) return omit()
        root.string("name", message.name)
    }

    override fun clanKickUser(message: ClanKickUser) {
        if (!filters[PropertyFilter.FRIENDCHAT_KICK]) return omit()
        root.string("name", message.name)
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
        if (!filters[PropertyFilter.AFFINEDCLANSETTINGS_SETMUTED_FROMCHANNEL]) return omit()
        root.string("name", message.name)
        root.int("clanid", message.channel)
        root.int("memberindex", message.member)
        root.int("muted", message.muted)
    }

    override fun faceSquare(message: FaceSquare) {
        if (!filters[PropertyFilter.FACE_SQUARE]) return omit()
        root.coordGrid(sessionState.level(), message.x, message.z)
    }

    override fun urlRequest(message: UrlRequest) {
        if (!filters[PropertyFilter.URL_REQUEST]) return omit()
        root.string("url", message.url)
        root.string("texta", message.textA)
        root.string("textb", message.textB)
        root.int("flags", message.flags)
    }

    override fun eventMouseClick(message: EventMouseClick) {
        if (!filters[PropertyFilter.EVENT_MOUSE_CLICK]) return omit()
        root.int("packedposition", message.packedPosition)
        root.int("buttonanddelta", message.buttonAndDelta)
    }

    override fun clanJoinChatLeaveChat(message: ClanJoinChatLeaveChat) {
        if (!filters[PropertyFilter.FRIENDCHAT_JOIN_LEAVE]) return omit()
        val name = message.name
        if (name == null) {
            root.boolean("leave", true)
        } else {
            root.string("name", name)
        }
    }

    override fun createLogProgress(message: CreateLogProgress) {
        if (!filters[PropertyFilter.CREATE_LOG_PROGRESS]) return omit()
        root.int("progress", message.progress)
    }

    override fun ignoreSetNotes(message: IgnoreSetNotes) {
        if (!filters[PropertyFilter.IGNORE_SETNOTES]) return omit()
        root.string("note", message.note)
        root.string("name", message.name)
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
        if (!filters[PropertyFilter.AFFINEDCLANSETTINGS_ADDBANNED_FROMCHANNEL]) return omit()
        root.string("name", message.name)
        root.int("clanid", message.channel)
        root.int("memberindex", message.member)
    }

    override fun friendSetNotes(message: FriendSetNotes) {
        if (!filters[PropertyFilter.FRIEND_SETNOTES]) return omit()
        root.string("name", message.name)
        root.string("note", message.note)
    }

    override fun sendPingReply(message: SendPingReply) {
        if (!filters[PropertyFilter.SEND_PING_REPLY]) return omit()
        root.int("fps", message.fps)
        root.int("value1", message.challengeA)
        root.int("value2", message.challengeB)
    }

    override fun friendSetRank(message: FriendSetRank) {
        if (!filters[PropertyFilter.FRIENDCHAT_SETRANK]) return omit()
        root.string("name", message.name)
        root.int("rank", message.rank)
    }

    override fun worldListFetch(message: WorldListFetch) {
        if (!filters[PropertyFilter.WORLDLIST_FETCH]) return omit()
        root.int("crc", message.crc)
    }

    override fun midiSongStop(message: MidiSongStop) {
        if (!filters[PropertyFilter.MIDI_SONG_STOP_CLIENT]) return omit()
        root.scriptVarType("song", ScriptVarType.MIDI, message.song)
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
        if (!filters[PropertyFilter.IGNORELIST_ADD]) return omit()
        root.string("name", message.name)
        root.int("temporary", message.temporary)
    }

    override fun ifTextChange(message: IfTextChange) {
        if (!filters[PropertyFilter.IF_TEXT_CHANGE]) return omit()
        root.component("com", message.combinedId)
        root.int("sub", message.sub)
        root.string("text", message.text)
    }

    override fun ifValueChange32(message: IfValueChange32) {
        if (!filters[PropertyFilter.IF_VALUE_CHANGE_32]) return omit()
        root.int("value", message.value)
        root.int("sub", message.sub)
        root.component("com", message.combinedId)
        root.int("flags", message.flags)
    }

    override fun locSelectSubmit(message: LocSelectSubmit) {
        if (!filters[PropertyFilter.LOCSELECT_SUBMIT]) return omit()
        root.int("cursorstyle", message.cursorStyle)
        coordinates.append(root, "coord", CoordGrid(message.packedCoordinate))
    }

    override fun eventKeyboard(message: EventKeyboard) {
        if (!filters[PropertyFilter.EVENT_KEYBOARD]) return omit()
        // RS3 has a time delta for each key, not a single shared OSRS timestamp.
        root.group("KEYS") {
            for (event in message.events) {
                group("KEY") {
                    int("key", event.key)
                    int("lasttransmitted", event.delta)
                }
            }
        }
    }

    override fun eventMouseMove(message: EventMouseMove) {
        if (!filters[PropertyFilter.EVENT_MOUSE_MOVE]) return omit()
        root.int("meanremainder", message.meanRemainder)
        root.int("remainder", message.remainder)
        root.group("MOVEMENTS") {
            for (movement in message.movements) {
                group("MOVEMENT") {
                    // Preserve wire ticks (including initial marker 8191), not a fabricated timestamp.
                    int("ticks", movement.elapsedTicks)
                    when (movement) {
                        is MouseMovement.Delta -> {
                            int("dx", movement.dx)
                            int("dy", movement.dy)
                        }
                        is MouseMovement.Absolute -> {
                            int("x", movement.x)
                            int("y", movement.y)
                        }
                    }
                    movement.nativeFlags?.let { int("flags", it) }
                }
            }
        }
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
        if (!filters[PropertyFilter.EVENT_NATIVE_MOUSE_MOVE]) return omit()
        root.int("meanremainder", message.meanRemainder)
        root.int("remainder", message.remainder)
        root.group("MOVEMENTS") {
            for (movement in message.movements) {
                group("MOVEMENT") {
                    // Preserve wire ticks (including initial marker 8191), not a fabricated timestamp.
                    int("ticks", movement.elapsedTicks)
                    when (movement) {
                        is MouseMovement.Delta -> {
                            int("dx", movement.dx)
                            int("dy", movement.dy)
                        }
                        is MouseMovement.Absolute -> {
                            int("x", movement.x)
                            int("y", movement.y)
                        }
                    }
                    movement.nativeFlags?.let { int("flags", it) }
                }
            }
        }
    }

    override fun sendSnapshot(message: SendSnapshot) {
        if (!filters[PropertyFilter.SEND_SNAPSHOT]) return omit()
        root.string("name", message.name)
        root.int("categoryindex", message.categoryIndex)
        root.int("includeimage", message.includeImage)
        root.string("description", message.description)
    }

    override fun addNewEmailAddress(message: AddNewEmailAddress) {
        if (!filters[PropertyFilter.ADD_NEW_EMAIL_ADDRESS]) return omit()
        root.string("email", message.email)
        root.int("flags", message.flags)
    }

    override fun changeEmailAddress(message: ChangeEmailAddress) {
        if (!filters[PropertyFilter.CHANGE_EMAIL_ADDRESS]) return omit()
        root.string("newemail", message.newEmail)
        root.string("oldemail", message.oldEmail)
    }

    override fun uidPassportResendRequest(message: UidPassportResendRequest) {
        if (!filters[PropertyFilter.UID_PASSPORT_RESEND_REQUEST]) return omit()
    }

    override fun messageQuickchatPublic(message: MessageQuickchatPublic) {
        if (!filters[PropertyFilter.MESSAGE_QUICKCHAT_PUBLIC]) return omit()
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun messageQuickchatPrivate(message: MessageQuickchatPrivate) {
        if (!filters[PropertyFilter.MESSAGE_QUICKCHAT_PRIVATE_CLIENT]) return omit()
        root.string("recipient", message.recipient)
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun storeServerPermVarcs(message: StoreServerPermVarcs) {
        if (!filters[PropertyFilter.STORE_SERVERPERM_VARCS]) return omit()
        root.int("complete", message.complete)
        root.appendVariables(message.variables, Rs3VariableDomain.CLIENT)
    }

    override fun clientDetailOptionsStatus(message: ClientDetailOptionsStatus) {
        if (!filters[PropertyFilter.CLIENT_DETAILOPTIONS_STATUS]) return omit()
        root.int("version", message.version)
        root.int("compatibility1", message.compatibility1)
        root.int("compatibility2", message.compatibility2)
        root.int("setting0", message.setting0)
        root.int("setting1", message.setting1)
        root.int("setting2", message.setting2)
        root.int("compatibility6", message.compatibility6)
        root.int("setting3", message.setting3)
        root.int("setting4", message.setting4)
        root.int("compatibility9", message.compatibility9)
        root.int("setting5", message.setting5)
        root.int("setting6", message.setting6)
        root.int("compatibility12", message.compatibility12)
        root.int("compatibility13", message.compatibility13)
        root.int("compatibility14", message.compatibility14)
        root.int("setting7", message.setting7)
        root.int("compatibility16", message.compatibility16)
        root.int("compatibility17", message.compatibility17)
        root.int("compatibility18", message.compatibility18)
        root.int("setting8", message.setting8)
        root.int("compatibility20", message.compatibility20)
        root.int("compatibility21", message.compatibility21)
        root.int("compatibility22", message.compatibility22)
        root.int("setting9", message.setting9)
        root.int("compatibility24", message.compatibility24)
        root.int("compatibility25", message.compatibility25)
        root.int("compatibility26", message.compatibility26)
        root.int("setting10", message.setting10)
        root.int("setting11", message.setting11)
        root.int("setting12", message.setting12)
        root.int("setting13", message.setting13)
        root.int("setting14", message.setting14)
        root.int("setting15", message.setting15)
        root.int("setting16", message.setting16)
        root.int("setting17", message.setting17)
        root.int("setting18", message.setting18)
        root.int("setting19", message.setting19)
        root.int("setting20", message.setting20)
        root.int("setting21", message.setting21)
        root.int("setting22", message.setting22)
        root.int("setting23", message.setting23)
        root.int("setting24", message.setting24)
        root.int("compatibility46", message.compatibility46)
        root.int("compatibility47", message.compatibility47)
        root.int("compatibility48", message.compatibility48)
        root.int("compatibility49", message.compatibility49)
        root.int("compatibility50", message.compatibility50)
        root.int("compatibility51", message.compatibility51)
        root.int("setting25", message.setting25)
        root.int("setting26", message.setting26)
        root.int("setting27", message.setting27)
        root.int("setting28", message.setting28)
        root.int("setting29", message.setting29)
        root.int("compatibility57", message.compatibility57)
    }

    override fun unknownClientOpcode(message: UnknownClientPacket) {
        if (!filters[PropertyFilter.UNKNOWN_CLIENT_OPCODE_HEX]) return omit()
        if (message is RawUnknownClientPacket) {
            message.decodeFailure?.let { root.string("decodefailure", it) }
        }
        root.int("opcode", message.opcode)
        root.string("name", message.name)
        root.any("bytes", message.bytes.joinToString(" ") { "%02x".format(it) })
    }
}
