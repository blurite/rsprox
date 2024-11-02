package net.rsprox.transcriber.indexer

import net.rsprot.protocol.util.CombinedId
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.incoming.model.buttons.If1Button
import net.rsprox.protocol.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonD
import net.rsprox.protocol.game.incoming.model.buttons.IfButtonT
import net.rsprox.protocol.game.incoming.model.buttons.IfSubOp
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelFullRequest
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelKickUser
import net.rsprox.protocol.game.incoming.model.clan.ClanSettingsFullRequest
import net.rsprox.protocol.game.incoming.model.events.EventAppletFocus
import net.rsprox.protocol.game.incoming.model.events.EventCameraPosition
import net.rsprox.protocol.game.incoming.model.events.EventKeyboard
import net.rsprox.protocol.game.incoming.model.events.EventMouseClick
import net.rsprox.protocol.game.incoming.model.events.EventMouseMove
import net.rsprox.protocol.game.incoming.model.events.EventMouseScroll
import net.rsprox.protocol.game.incoming.model.events.EventNativeMouseClick
import net.rsprox.protocol.game.incoming.model.events.EventNativeMouseMove
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatJoinLeave
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatKick
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatSetRank
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.game.incoming.model.misc.client.ConnectionTelemetry
import net.rsprox.protocol.game.incoming.model.misc.client.DetectModifiedClient
import net.rsprox.protocol.game.incoming.model.misc.client.Idle
import net.rsprox.protocol.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.game.incoming.model.misc.client.MembershipPromotionEligibility
import net.rsprox.protocol.game.incoming.model.misc.client.NoTimeout
import net.rsprox.protocol.game.incoming.model.misc.client.ReflectionCheckReply
import net.rsprox.protocol.game.incoming.model.misc.client.SendPingReply
import net.rsprox.protocol.game.incoming.model.misc.client.SoundJingleEnd
import net.rsprox.protocol.game.incoming.model.misc.client.WindowStatus
import net.rsprox.protocol.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.game.incoming.model.misc.user.CloseModal
import net.rsprox.protocol.game.incoming.model.misc.user.HiscoreRequest
import net.rsprox.protocol.game.incoming.model.misc.user.IfCrmViewClick
import net.rsprox.protocol.game.incoming.model.misc.user.MoveGameClick
import net.rsprox.protocol.game.incoming.model.misc.user.MoveMinimapClick
import net.rsprox.protocol.game.incoming.model.misc.user.OculusLeave
import net.rsprox.protocol.game.incoming.model.misc.user.SendSnapshot
import net.rsprox.protocol.game.incoming.model.misc.user.SetChatFilterSettings
import net.rsprox.protocol.game.incoming.model.misc.user.SetHeading
import net.rsprox.protocol.game.incoming.model.misc.user.Teleport
import net.rsprox.protocol.game.incoming.model.misc.user.UpdatePlayerModelV1
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc
import net.rsprox.protocol.game.incoming.model.npcs.OpNpc6
import net.rsprox.protocol.game.incoming.model.npcs.OpNpcT
import net.rsprox.protocol.game.incoming.model.objs.OpObj
import net.rsprox.protocol.game.incoming.model.objs.OpObj6
import net.rsprox.protocol.game.incoming.model.objs.OpObjT
import net.rsprox.protocol.game.incoming.model.players.OpPlayer
import net.rsprox.protocol.game.incoming.model.players.OpPlayerT
import net.rsprox.protocol.game.incoming.model.resumed.ResumePCountDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePNameDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePObjDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePStringDialog
import net.rsprox.protocol.game.incoming.model.resumed.ResumePauseButton
import net.rsprox.protocol.game.incoming.model.social.FriendListAdd
import net.rsprox.protocol.game.incoming.model.social.FriendListDel
import net.rsprox.protocol.game.incoming.model.social.IgnoreListAdd
import net.rsprox.protocol.game.incoming.model.social.IgnoreListDel
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAt
import net.rsprox.protocol.game.outgoing.model.camera.CamLookAtEasedCoord
import net.rsprox.protocol.game.outgoing.model.camera.CamMode
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveTo
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToArc
import net.rsprox.protocol.game.outgoing.model.camera.CamMoveToCycles
import net.rsprox.protocol.game.outgoing.model.camera.CamReset
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateBy
import net.rsprox.protocol.game.outgoing.model.camera.CamRotateTo
import net.rsprox.protocol.game.outgoing.model.camera.CamShake
import net.rsprox.protocol.game.outgoing.model.camera.CamSmoothReset
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV1
import net.rsprox.protocol.game.outgoing.model.camera.CamTargetV2
import net.rsprox.protocol.game.outgoing.model.camera.OculusSync
import net.rsprox.protocol.game.outgoing.model.clan.ClanChannelDelta
import net.rsprox.protocol.game.outgoing.model.clan.ClanChannelFull
import net.rsprox.protocol.game.outgoing.model.clan.ClanSettingsDelta
import net.rsprox.protocol.game.outgoing.model.clan.ClanSettingsFull
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannel
import net.rsprox.protocol.game.outgoing.model.clan.MessageClanChannelSystem
import net.rsprox.protocol.game.outgoing.model.clan.VarClan
import net.rsprox.protocol.game.outgoing.model.clan.VarClanDisable
import net.rsprox.protocol.game.outgoing.model.clan.VarClanEnable
import net.rsprox.protocol.game.outgoing.model.friendchat.MessageFriendChannel
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFullV1
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelFullV2
import net.rsprox.protocol.game.outgoing.model.friendchat.UpdateFriendChatChannelSingleUser
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcUpdateType
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.SetNpcUpdateOrigin
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.BaseAnimationSetExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.BodyCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.CombatLevelChangeExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.EnabledOpsExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.FaceCoordExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.HeadCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.HeadIconCustomisationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.NameChangeExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.OldSpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.TransformationExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.PlayerUpdateType
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.AppearanceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.ChatExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.FaceAngleExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.MoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.NameExtrasExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.TemporaryMoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExactMoveExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FacePathingEntityExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.HitExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SayExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SequenceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.TintingExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV1
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV2
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityInfoV3
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.WorldEntityUpdateType
import net.rsprox.protocol.game.outgoing.model.interfaces.IfClearInv
import net.rsprox.protocol.game.outgoing.model.interfaces.IfCloseSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfMoveSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenSub
import net.rsprox.protocol.game.outgoing.model.interfaces.IfOpenTop
import net.rsprox.protocol.game.outgoing.model.interfaces.IfResync
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAngle
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetAnim
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetColour
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetEvents
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetHide
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetModel
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHead
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetNpcHeadActive
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetObject
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerHead
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBaseColour
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelBodyType
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelObj
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPlayerModelSelf
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetPosition
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetRotateSpeed
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetScrollPos
import net.rsprox.protocol.game.outgoing.model.interfaces.IfSetText
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.game.outgoing.model.logout.Logout
import net.rsprox.protocol.game.outgoing.model.logout.LogoutTransfer
import net.rsprox.protocol.game.outgoing.model.logout.LogoutWithReason
import net.rsprox.protocol.game.outgoing.model.map.RebuildLogin
import net.rsprox.protocol.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV1
import net.rsprox.protocol.game.outgoing.model.map.RebuildWorldEntityV2
import net.rsprox.protocol.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.game.outgoing.model.misc.client.HideLocOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HideNpcOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HideObjOps
import net.rsprox.protocol.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.game.outgoing.model.misc.client.HiscoreReply
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.client.ReflectionChecker
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.game.outgoing.model.misc.client.ResetInteractionMode
import net.rsprox.protocol.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.game.outgoing.model.misc.client.ServerTickEnd
import net.rsprox.protocol.game.outgoing.model.misc.client.SetHeatmapEnabled
import net.rsprox.protocol.game.outgoing.model.misc.client.SetInteractionMode
import net.rsprox.protocol.game.outgoing.model.misc.client.SiteSettings
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.game.outgoing.model.misc.client.UpdateUid192
import net.rsprox.protocol.game.outgoing.model.misc.client.UrlOpen
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettings
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.game.outgoing.model.misc.player.MessageGame
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.game.outgoing.model.misc.player.SetMapFlag
import net.rsprox.protocol.game.outgoing.model.misc.player.SetPlayerOp
import net.rsprox.protocol.game.outgoing.model.misc.player.TriggerOnDialogAbort
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunEnergy
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateRunWeight
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV1
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatV2
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStockMarketSlot
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateTradingPost
import net.rsprox.protocol.game.outgoing.model.social.FriendListLoaded
import net.rsprox.protocol.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.game.outgoing.model.social.UpdateFriendList
import net.rsprox.protocol.game.outgoing.model.social.UpdateIgnoreList
import net.rsprox.protocol.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongStop
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongV1
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongV2
import net.rsprox.protocol.game.outgoing.model.sound.MidiSongWithSecondary
import net.rsprox.protocol.game.outgoing.model.sound.MidiSwap
import net.rsprox.protocol.game.outgoing.model.sound.SynthSound
import net.rsprox.protocol.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.MapAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcHeadIconSpecific
import net.rsprox.protocol.game.outgoing.model.specific.NpcSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.PlayerSpotAnimSpecific
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.game.outgoing.model.specific.ProjAnimSpecificV3
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownString
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.game.outgoing.model.varp.VarpReset
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync
import net.rsprox.protocol.game.outgoing.model.worldentity.ClearEntities
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorld
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAddChange
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.LocMerge
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.MapProjAnim
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjAdd
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjCount
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjCustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjDel
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjEnabledOps
import net.rsprox.protocol.game.outgoing.model.zone.payload.ObjUncustomise
import net.rsprox.protocol.game.outgoing.model.zone.payload.SoundArea
import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.indexing.BinaryIndex
import net.rsprox.shared.indexing.IndexedType
import net.rsprox.shared.indexing.increment
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.prot.Prot
import net.rsprox.transcriber.state.Npc
import net.rsprox.transcriber.state.Player
import net.rsprox.transcriber.state.StateTracker

@Suppress("DuplicatedCode")
public class IndexerTranscriber private constructor(
    private val stateTracker: StateTracker,
    cacheProvider: CacheProvider,
    override val monitor: SessionMonitor<*>,
    private val binaryIndex: BinaryIndex,
) : Transcriber {
    public constructor(
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
        stateTracker: StateTracker,
        binaryIndex: BinaryIndex,
    ) : this(
        stateTracker,
        cacheProvider,
        monitor,
        binaryIndex,
    )

    private fun getNpcInAnyWorld(index: Int): Npc? {
        for (world in stateTracker.getAllWorlds()) {
            val npc = world.getNpcOrNull(index)
            if (npc != null) {
                return npc
            }
        }
        return null
    }

    private var lastConnection: Int = 0

    override val cache: Cache = cacheProvider.get()

    override fun setCurrentProt(prot: Prot) {
        stateTracker.currentProt = prot.toString()
    }

    override fun onTranscribeStart() {
    }

    override fun onTranscribeEnd() {
    }

    private fun incrementComponent(combinedId: CombinedId) {
        incrementInterfaceId(combinedId.interfaceId)
    }

    private fun incrementInterfaceId(interfaceId: Int) {
        if (interfaceId == -1) return
        binaryIndex.increment(IndexedType.INTERFACE, interfaceId)
    }

    override fun if1Button(message: If1Button) {
        incrementInterfaceId(message.interfaceId)
    }

    override fun if3Button(message: If3Button) {
        incrementInterfaceId(message.interfaceId)
    }

    override fun ifSubOp(message: IfSubOp) {
        incrementInterfaceId(message.interfaceId)
    }

    override fun ifButtonD(message: IfButtonD) {
        incrementInterfaceId(message.selectedInterfaceId)
        incrementInterfaceId(message.targetInterfaceId)
    }

    override fun ifButtonT(message: IfButtonT) {
        incrementInterfaceId(message.selectedInterfaceId)
        incrementInterfaceId(message.targetInterfaceId)
    }

    override fun affinedClanSettingsAddBannedFromChannel(message: AffinedClanSettingsAddBannedFromChannel) {
    }

    override fun affinedClanSettingsSetMutedFromChannel(message: AffinedClanSettingsSetMutedFromChannel) {
    }

    override fun clanChannelFullRequest(message: ClanChannelFullRequest) {
    }

    override fun clanChannelKickUser(message: ClanChannelKickUser) {
    }

    override fun clanSettingsFullRequest(message: ClanSettingsFullRequest) {
    }

    override fun eventAppletFocus(message: EventAppletFocus) {
    }

    override fun eventCameraPosition(message: EventCameraPosition) {
    }

    override fun eventKeyboard(message: EventKeyboard) {
    }

    override fun eventMouseClick(message: EventMouseClick) {
    }

    override fun eventMouseMove(message: EventMouseMove) {
    }

    override fun eventMouseScroll(message: EventMouseScroll) {
    }

    override fun eventNativeMouseClick(message: EventNativeMouseClick) {
    }

    override fun eventNativeMouseMove(message: EventNativeMouseMove) {
    }

    override fun friendChatJoinLeave(message: FriendChatJoinLeave) {
    }

    override fun friendChatKick(message: FriendChatKick) {
    }

    override fun friendChatSetRank(message: FriendChatSetRank) {
    }

    override fun opLoc(message: OpLoc) {
        binaryIndex.increment(IndexedType.LOC, message.id)
    }

    override fun opLoc6(message: OpLoc6) {
        binaryIndex.increment(IndexedType.LOC, message.id)
    }

    override fun opLocT(message: OpLocT) {
        binaryIndex.increment(IndexedType.LOC, message.id)
    }

    override fun messagePrivateClient(message: MessagePrivate) {
    }

    override fun messagePublic(message: MessagePublic) {
    }

    override fun detectModifiedClient(message: DetectModifiedClient) {
    }

    override fun idle(message: Idle) {
    }

    override fun mapBuildComplete(message: MapBuildComplete) {
    }

    override fun membershipPromotionEligibility(message: MembershipPromotionEligibility) {
    }

    override fun noTimeout(message: NoTimeout) {
    }

    override fun reflectionCheckReply(message: ReflectionCheckReply) {
    }

    override fun sendPingReply(message: SendPingReply) {
    }

    override fun soundJingleEnd(message: SoundJingleEnd) {
    }

    override fun connectionTelemetry(message: ConnectionTelemetry) {
    }

    override fun windowStatus(message: WindowStatus) {
    }

    override fun bugReport(message: BugReport) {
    }

    override fun clickWorldMap(message: ClickWorldMap) {
    }

    override fun clientCheat(message: ClientCheat) {
    }

    override fun closeModal(message: CloseModal) {
    }

    override fun hiscoreRequest(message: HiscoreRequest) {
    }

    override fun ifCrmViewClick(message: IfCrmViewClick) {
    }

    override fun moveGameClick(message: MoveGameClick) {
    }

    override fun moveMinimapClick(message: MoveMinimapClick) {
    }

    override fun oculusLeave(message: OculusLeave) {
    }

    override fun sendSnapshot(message: SendSnapshot) {
    }

    override fun setChatFilterSettings(message: SetChatFilterSettings) {
    }

    override fun teleport(message: Teleport) {
    }

    override fun updatePlayerModelV1(message: UpdatePlayerModelV1) {
    }

    override fun opNpc(message: OpNpc) {
        val npc = getNpcInAnyWorld(message.index) ?: return
        binaryIndex.increment(IndexedType.NPC, npc.id)
    }

    override fun opNpc6(message: OpNpc6) {
        binaryIndex.increment(IndexedType.NPC, message.id)
    }

    override fun opNpcT(message: OpNpcT) {
        val npc = getNpcInAnyWorld(message.index) ?: return
        binaryIndex.increment(IndexedType.NPC, npc.id)
    }

    override fun opObj(message: OpObj) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun opObj6(message: OpObj6) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun opObjT(message: OpObjT) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun opPlayer(message: OpPlayer) {
    }

    override fun opPlayerT(message: OpPlayerT) {
    }

    override fun resumePauseButton(message: ResumePauseButton) {
        incrementComponent(CombinedId(message.interfaceId, message.componentId))
    }

    override fun resumePCountDialog(message: ResumePCountDialog) {
    }

    override fun resumePNameDialog(message: ResumePNameDialog) {
    }

    override fun resumePObjDialog(message: ResumePObjDialog) {
    }

    override fun resumePStringDialog(message: ResumePStringDialog) {
    }

    override fun friendListAdd(message: FriendListAdd) {
    }

    override fun friendListDel(message: FriendListDel) {
    }

    override fun ignoreListAdd(message: IgnoreListAdd) {
    }

    override fun ignoreListDel(message: IgnoreListDel) {
    }

    override fun setHeading(message: SetHeading) {
    }

    override fun camLookAt(message: CamLookAt) {
    }

    override fun camLookAtEasedCoord(message: CamLookAtEasedCoord) {
    }

    override fun camMode(message: CamMode) {
    }

    override fun camMoveTo(message: CamMoveTo) {
    }

    override fun camMoveToArc(message: CamMoveToArc) {
    }

    override fun camMoveToCycles(message: CamMoveToCycles) {
    }

    override fun camReset(message: CamReset) {
    }

    override fun camRotateBy(message: CamRotateBy) {
    }

    override fun camRotateTo(message: CamRotateTo) {
    }

    override fun camShake(message: CamShake) {
    }

    override fun camSmoothReset(message: CamSmoothReset) {
    }

    override fun camTargetV2(message: CamTargetV2) {
    }

    override fun camTargetV1(message: CamTargetV1) {
    }

    override fun oculusSync(message: OculusSync) {
    }

    override fun clanChannelDelta(message: ClanChannelDelta) {
    }

    override fun clanChannelFull(message: ClanChannelFull) {
    }

    override fun clanSettingsDelta(message: ClanSettingsDelta) {
    }

    override fun clanSettingsFull(message: ClanSettingsFull) {
    }

    override fun messageClanChannel(message: MessageClanChannel) {
    }

    override fun messageClanChannelSystem(message: MessageClanChannelSystem) {
    }

    override fun varClan(message: VarClan) {
    }

    override fun varClanDisable(message: VarClanDisable) {
    }

    override fun varClanEnable(message: VarClanEnable) {
    }

    override fun messageFriendChannel(message: MessageFriendChannel) {
    }

    override fun updateFriendChatChannelFullV1(message: UpdateFriendChatChannelFullV1) {
    }

    override fun updateFriendChatChannelFullV2(message: UpdateFriendChatChannelFullV2) {
    }

    override fun updateFriendChatChannelSingleUser(message: UpdateFriendChatChannelSingleUser) {
    }

    override fun setNpcUpdateOrigin(message: SetNpcUpdateOrigin) {
    }

    private fun preWorldEntityUpdate(message: WorldEntityInfo) {
        for ((index, update) in message.updates) {
            when (update) {
                is WorldEntityUpdateType.ActiveV2 -> {
                }
                WorldEntityUpdateType.HighResolutionToLowResolution -> {
                }
                is WorldEntityUpdateType.LowResolutionToHighResolutionV2 -> {
                    val world = stateTracker.createWorld(index)
                    world.sizeX = update.sizeX
                    world.sizeZ = update.sizeZ
                    world.angle = update.angle
                    world.level = update.level
                    world.coordFine = update.coordFine
                    world.coord = update.coordFine.toCoordGrid(world.level)
                }
                WorldEntityUpdateType.Idle -> {
                    // noop
                }
                is WorldEntityUpdateType.ActiveV1 -> {
                    throw IllegalStateException("Invalid update: $update")
                }
                is WorldEntityUpdateType.LowResolutionToHighResolutionV1 -> {
                    throw IllegalStateException("Invalid update: $update")
                }
            }
        }
    }

    private fun postWorldEntityUpdate(message: WorldEntityInfo) {
        for ((index, update) in message.updates) {
            when (update) {
                is WorldEntityUpdateType.ActiveV2 -> {
                    val world = stateTracker.getWorld(index)
                    world.angle = update.angle
                    world.coordFine = update.coordFine
                    world.coord = update.coordFine.toCoordGrid(world.level)
                }
                WorldEntityUpdateType.HighResolutionToLowResolution -> {
                    stateTracker.destroyWorld(index)
                }
                is WorldEntityUpdateType.LowResolutionToHighResolutionV2 -> {
                }
                WorldEntityUpdateType.Idle -> {
                    // noop
                }
                is WorldEntityUpdateType.ActiveV1 -> {
                    throw IllegalStateException("Invalid update: $update")
                }
                is WorldEntityUpdateType.LowResolutionToHighResolutionV1 -> {
                    throw IllegalStateException("Invalid update: $update")
                }
            }
        }
    }

    override fun worldEntityInfoV1(message: WorldEntityInfoV1) {
        preWorldEntityUpdate(message)
        postWorldEntityUpdate(message)
    }

    override fun worldEntityInfoV2(message: WorldEntityInfoV2) {
        preWorldEntityUpdate(message)
        postWorldEntityUpdate(message)
    }

    override fun worldEntityInfoV3(message: WorldEntityInfoV3) {
        preWorldEntityUpdate(message)
        postWorldEntityUpdate(message)
    }

    override fun ifClearInv(message: IfClearInv) {
        incrementInterfaceId(message.interfaceId)
    }

    override fun ifCloseSub(message: IfCloseSub) {
        incrementComponent(message.combinedId)
        stateTracker.closeInterface(message.combinedId)
    }

    override fun ifMoveSub(message: IfMoveSub) {
        incrementComponent(message.sourceCombinedId)
        incrementComponent(message.destinationCombinedId)
        stateTracker.moveInterface(message.sourceCombinedId, message.destinationCombinedId)
    }

    override fun ifOpenSub(message: IfOpenSub) {
        incrementInterfaceId(message.interfaceId)
        incrementComponent(message.destinationCombinedId)
        stateTracker.openInterface(message.interfaceId, message.destinationCombinedId)
    }

    override fun ifOpenTop(message: IfOpenTop) {
        incrementInterfaceId(message.interfaceId)
        stateTracker.toplevelInterface = message.interfaceId
    }

    override fun ifResync(message: IfResync) {
        stateTracker.toplevelInterface = message.topLevelInterface
        for (sub in message.subInterfaces) {
            stateTracker.openInterface(sub.interfaceId, sub.destinationCombinedId)
        }
    }

    override fun ifSetAngle(message: IfSetAngle) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetAnim(message: IfSetAnim) {
        incrementComponent(message.combinedId)
        binaryIndex.increment(IndexedType.SEQ, message.anim)
    }

    override fun ifSetColour(message: IfSetColour) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetEvents(message: IfSetEvents) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetHide(message: IfSetHide) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetModel(message: IfSetModel) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
        incrementComponent(message.combinedId)
        binaryIndex.increment(IndexedType.NPC, message.npc)
    }

    override fun ifSetNpcHeadActive(message: IfSetNpcHeadActive) {
        incrementComponent(message.combinedId)
        val npc = getNpcInAnyWorld(message.index) ?: return
        binaryIndex.increment(IndexedType.NPC, npc.id)
    }

    override fun ifSetObject(message: IfSetObject) {
        incrementComponent(message.combinedId)
        if (message.obj != -1) {
            binaryIndex.increment(IndexedType.OBJ, message.obj)
        }
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetPlayerModelBaseColour(message: IfSetPlayerModelBaseColour) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetPlayerModelBodyType(message: IfSetPlayerModelBodyType) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetPlayerModelObj(message: IfSetPlayerModelObj) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetPosition(message: IfSetPosition) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetRotateSpeed(message: IfSetRotateSpeed) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetText(message: IfSetText) {
        incrementComponent(message.combinedId)
        binaryIndex.increment(IndexedType.TEXT, message.text)
    }

    private fun isInvExcludedFromObjs(invId: Int): Boolean {
        // Skip trade, inventory, equipment, bank, ge
        return invId == 90 ||
            invId == 93 ||
            invId == 94 ||
            invId == 95 ||
            invId in 518..523 ||
            invId in 539..540
    }

    override fun updateInvFull(message: UpdateInvFull) {
        incrementComponent(message.combinedId)
        if (!isInvExcludedFromObjs(message.inventoryId)) {
            for (obj in message.objs) {
                if (obj.id != -1) {
                    binaryIndex.increment(IndexedType.OBJ, obj.id)
                }
            }
        }
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
        incrementComponent(message.combinedId)
        if (!isInvExcludedFromObjs(message.inventoryId)) {
            for (obj in message.objs) {
                if (obj.id != -1) {
                    binaryIndex.increment(IndexedType.OBJ, obj.id)
                }
            }
        }
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
    }

    override fun logout(message: Logout) {
    }

    override fun logoutTransfer(message: LogoutTransfer) {
    }

    override fun logoutWithReason(message: LogoutWithReason) {
    }

    override fun reconnect(message: Reconnect) {
        lastConnection = stateTracker.cycle
    }

    override fun rebuildLogin(message: RebuildLogin) {
        stateTracker.overridePlayer(
            Player(
                message.playerInfoInitBlock.localPlayerIndex,
                "uninitialized",
                message.playerInfoInitBlock.localPlayerCoord,
            ),
        )
        stateTracker.localPlayerIndex = message.playerInfoInitBlock.localPlayerIndex
        val world = stateTracker.createWorld(-1)
        world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))

        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        for (mapsquareX in minMapsquareX..maxMapsquareX) {
            for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                binaryIndex.increment(IndexedType.MAPSQUARE, mapsquareId)
            }
        }
    }

    override fun rebuildNormal(message: RebuildNormal) {
        val world = stateTracker.getWorld(-1)
        world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))

        val minMapsquareX = (message.zoneX - 6) ushr 3
        val maxMapsquareX = (message.zoneX + 6) ushr 3
        val minMapsquareZ = (message.zoneZ - 6) ushr 3
        val maxMapsquareZ = (message.zoneZ + 6) ushr 3
        for (mapsquareX in minMapsquareX..maxMapsquareX) {
            for (mapsquareZ in minMapsquareZ..maxMapsquareZ) {
                val mapsquareId = (mapsquareX shl 8) or mapsquareZ
                binaryIndex.increment(IndexedType.MAPSQUARE, mapsquareId)
            }
        }
    }

    override fun rebuildRegion(message: RebuildRegion) {
        val world = stateTracker.getWorld(-1)
        world.rebuild(CoordGrid(0, (message.zoneX - 6) shl 3, (message.zoneZ - 6) shl 3))

        val startZoneX = message.zoneX - 6
        val startZoneZ = message.zoneZ - 6
        val mapsquares = mutableSetOf<Int>()
        for (level in 0..<4) {
            for (zoneX in startZoneX..(message.zoneX + 6)) {
                for (zoneZ in startZoneZ..(message.zoneZ + 6)) {
                    val block = message.buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                    // Invalid zone
                    if (block.mapsquareId == 32767) continue
                    mapsquares += block.mapsquareId
                }
            }
        }
        for (mapsquare in mapsquares) {
            binaryIndex.increment(IndexedType.MAPSQUARE, mapsquare)
        }
    }

    override fun rebuildWorldEntityV1(message: RebuildWorldEntityV1) {
        throw IllegalStateException("Invalid message: $message")
    }

    override fun rebuildWorldEntityV2(message: RebuildWorldEntityV2) {
        val world = stateTracker.getWorld(message.index)
        world.rebuild(CoordGrid(0, (message.baseX - 6) shl 3, (message.baseZ - 6) shl 3))

        val startZoneX = message.baseX - 6
        val startZoneZ = message.baseZ - 6
        val mapsquares = mutableSetOf<Int>()
        for (level in 0..<4) {
            for (zoneX in startZoneX..(message.baseX + 6)) {
                for (zoneZ in startZoneZ..(message.baseZ + 6)) {
                    val block = message.buildArea[level, zoneX - startZoneX, zoneZ - startZoneZ]
                    // Invalid zone
                    if (block.mapsquareId == 32767) continue
                    mapsquares += block.mapsquareId
                }
            }
        }
        for (mapsquare in mapsquares) {
            binaryIndex.increment(IndexedType.MAPSQUARE, mapsquare)
        }
    }

    override fun hideLocOps(message: HideLocOps) {
    }

    override fun hideNpcOps(message: HideNpcOps) {
    }

    override fun hideObjOps(message: HideObjOps) {
    }

    override fun hintArrow(message: HintArrow) {
    }

    override fun hiscoreReply(message: HiscoreReply) {
    }

    override fun minimapToggle(message: MinimapToggle) {
    }

    override fun reflectionChecker(message: ReflectionChecker) {
    }

    override fun resetAnims(message: ResetAnims) {
    }

    override fun sendPing(message: SendPing) {
    }

    override fun serverTickEnd(message: ServerTickEnd) {
        stateTracker.incrementCycle()
    }

    override fun setHeatmapEnabled(message: SetHeatmapEnabled) {
    }

    override fun siteSettings(message: SiteSettings) {
    }

    override fun updateRebootTimer(message: UpdateRebootTimer) {
    }

    override fun updateUid192(message: UpdateUid192) {
    }

    override fun urlOpen(message: UrlOpen) {
    }

    override fun chatFilterSettings(message: ChatFilterSettings) {
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
    }

    override fun messageGame(message: MessageGame) {
        binaryIndex.increment(IndexedType.MESSAGE_GAME, message.message)
    }

    override fun runClientScript(message: RunClientScript) {
        binaryIndex.increment(IndexedType.CLIENTSCRIPTS, message.id)
        for (i in message.types.indices) {
            val char = message.types[i]
            val type =
                ScriptVarType.entries.first { type ->
                    type.char == char
                }
            if (type == ScriptVarType.STRING) {
                binaryIndex.increment(IndexedType.TEXT, message.values[i].toString())
            }
            if (type.baseVarType != BaseVarType.INTEGER) {
                continue
            }
            val value = message.values[i].toString().toInt()
            if (value == -1) {
                continue
            }
            val indexedType =
                when (type) {
                    ScriptVarType.NPC -> IndexedType.NPC
                    ScriptVarType.OBJ -> IndexedType.OBJ
                    ScriptVarType.VERIFY_OBJECT -> IndexedType.OBJ
                    ScriptVarType.INTERFACE -> IndexedType.INTERFACE
                    ScriptVarType.SEQ -> IndexedType.SEQ
                    ScriptVarType.SPOTANIM -> IndexedType.SPOTANIM
                    ScriptVarType.MIDI -> IndexedType.MIDI
                    ScriptVarType.SYNTH -> IndexedType.SYNTH
                    ScriptVarType.JINGLE -> IndexedType.JINGLE
                    else -> continue
                }
            binaryIndex.increment(indexedType, value)
        }
    }

    override fun setMapFlag(message: SetMapFlag) {
    }

    override fun setPlayerOp(message: SetPlayerOp) {
    }

    override fun triggerOnDialogAbort(message: TriggerOnDialogAbort) {
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
    }

    override fun updateStatV2(message: UpdateStatV2) {
    }

    override fun updateStatV1(message: UpdateStatV1) {
    }

    override fun updateStockMarketSlot(message: UpdateStockMarketSlot) {
    }

    override fun updateTradingPost(message: UpdateTradingPost) {
    }

    override fun friendListLoaded(message: FriendListLoaded) {
    }

    override fun messagePrivate(message: net.rsprox.protocol.game.outgoing.model.social.MessagePrivate) {
    }

    override fun messagePrivateEcho(message: MessagePrivateEcho) {
    }

    override fun updateFriendList(message: UpdateFriendList) {
    }

    override fun updateIgnoreList(message: UpdateIgnoreList) {
    }

    override fun midiJingle(message: MidiJingle) {
        binaryIndex.increment(IndexedType.JINGLE, message.id)
    }

    override fun midiSongV2(message: MidiSongV2) {
        binaryIndex.increment(IndexedType.MIDI, message.id)
    }

    override fun midiSongV1(message: MidiSongV1) {
        binaryIndex.increment(IndexedType.MIDI, message.id)
    }

    override fun midiSongStop(message: MidiSongStop) {
    }

    override fun midiSongWithSecondary(message: MidiSongWithSecondary) {
        binaryIndex.increment(IndexedType.MIDI, message.primaryId)
        binaryIndex.increment(IndexedType.MIDI, message.secondaryId)
    }

    override fun midiSwap(message: MidiSwap) {
    }

    override fun synthSound(message: SynthSound) {
        binaryIndex.increment(IndexedType.SYNTH, message.id)
    }

    override fun locAnimSpecific(message: LocAnimSpecific) {
        binaryIndex.increment(IndexedType.SEQ, message.id)
    }

    override fun mapAnimSpecific(message: MapAnimSpecific) {
        appendCheckedSpotanim(message.id)
    }

    override fun npcAnimSpecific(message: NpcAnimSpecific) {
        binaryIndex.increment(IndexedType.SEQ, message.id)
        val npc = getNpcInAnyWorld(message.index) ?: return
        binaryIndex.increment(IndexedType.NPC, npc.id)
    }

    override fun npcHeadIconSpecific(message: NpcHeadIconSpecific) {
        val npc = getNpcInAnyWorld(message.index) ?: return
        binaryIndex.increment(IndexedType.NPC, npc.id)
    }

    override fun npcSpotAnimSpecific(message: NpcSpotAnimSpecific) {
        appendCheckedSpotanim(message.id)
        val npc = getNpcInAnyWorld(message.index) ?: return
        binaryIndex.increment(IndexedType.NPC, npc.id)
    }

    override fun playerAnimSpecific(message: PlayerAnimSpecific) {
        binaryIndex.increment(IndexedType.SEQ, message.id)
    }

    override fun playerSpotAnimSpecific(message: PlayerSpotAnimSpecific) {
        appendCheckedSpotanim(message.id)
    }

    override fun projAnimSpecificV2(message: ProjAnimSpecificV2) {
        appendCheckedSpotanim(message.id)
    }

    override fun projAnimSpecificV3(message: ProjAnimSpecificV3) {
        appendCheckedSpotanim(message.id)
    }

    override fun varpLarge(message: VarpLarge) {
        logVarp(message.id, message.value)
    }

    override fun varpReset(message: VarpReset) {
    }

    override fun varpSmall(message: VarpSmall) {
        logVarp(message.id, message.value)
    }

    private fun getImpactedVarbits(
        basevar: Int,
        oldValue: Int,
        newValue: Int,
    ): List<VarBitType> {
        if (!stateTracker.varbitsLoaded()) {
            stateTracker.associateVarbits(cache.listVarBitTypes())
        }
        return stateTracker.getAssociatedVarbits(basevar).filter { type ->
            val bitcount = (type.endbit - type.startbit) + 1
            val bitmask = type.bitmask(bitcount)
            val oldVarbitValue = oldValue ushr type.startbit and bitmask
            val newVarbitValue = newValue ushr type.startbit and bitmask
            oldVarbitValue != newVarbitValue
        }
    }

    private fun logVarp(
        id: Int,
        newValue: Int,
    ) {
        val oldValue = stateTracker.getVarp(id)
        val impactedVarbits = getImpactedVarbits(id, oldValue, newValue)
        stateTracker.setVarp(id, newValue)
        // Ignore any varbits and varps set on tick 0
        if (stateTracker.cycle == lastConnection) {
            return
        }
        binaryIndex.increment(IndexedType.VARP, id)
        for (bit in impactedVarbits) {
            binaryIndex.increment(IndexedType.VARBIT, bit.id)
        }
    }

    override fun varpSync(message: VarpSync) {
    }

    override fun clearEntities(message: ClearEntities) {
        stateTracker.destroyDynamicWorlds()
    }

    override fun setActiveWorld(message: SetActiveWorld) {
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
        stateTracker.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
    }

    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        stateTracker.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
        for (update in message.packets) {
            when (update) {
                is LocAddChange -> {
                    binaryIndex.increment(IndexedType.LOC, update.id)
                }
                is LocAnim -> {
                    binaryIndex.increment(IndexedType.SEQ, update.id)
                }
                is LocDel -> {
                }
                is LocMerge -> {
                    binaryIndex.increment(IndexedType.LOC, update.id)
                }
                is MapAnim -> {
                    appendCheckedSpotanim(update.id)
                }
                is MapProjAnim -> {
                    appendCheckedSpotanim(update.id)
                }
                is ObjAdd -> {
                    binaryIndex.increment(IndexedType.OBJ, update.id)
                }
                is ObjCount -> {
                    binaryIndex.increment(IndexedType.OBJ, update.id)
                }
                is ObjDel -> {
                    binaryIndex.increment(IndexedType.OBJ, update.id)
                }
                is ObjEnabledOps -> {
                    binaryIndex.increment(IndexedType.OBJ, update.id)
                }
                is SoundArea -> {
                    binaryIndex.increment(IndexedType.SYNTH, update.id)
                }
            }
        }
    }

    override fun updateZonePartialFollows(message: UpdateZonePartialFollows) {
        stateTracker.getActiveWorld().setActiveZone(message.zoneX, message.zoneZ, message.level)
    }

    override fun locAddChange(message: LocAddChange) {
        binaryIndex.increment(IndexedType.LOC, message.id)
    }

    override fun locAnim(message: LocAnim) {
        binaryIndex.increment(IndexedType.SEQ, message.id)
    }

    override fun locDel(message: LocDel) {
    }

    override fun locMerge(message: LocMerge) {
        binaryIndex.increment(IndexedType.LOC, message.id)
    }

    override fun mapAnim(message: MapAnim) {
        appendCheckedSpotanim(message.id)
    }

    override fun mapProjAnim(message: MapProjAnim) {
        appendCheckedSpotanim(message.id)
    }

    override fun objAdd(message: ObjAdd) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objCount(message: ObjCount) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objDel(message: ObjDel) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objEnabledOps(message: ObjEnabledOps) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun soundArea(message: SoundArea) {
        binaryIndex.increment(IndexedType.SYNTH, message.id)
    }

    override fun unknownString(message: UnknownString) {
    }

    override fun objCustomise(message: ObjCustomise) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objUncustomise(message: ObjUncustomise) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun setInteractionMode(message: SetInteractionMode) {
    }

    override fun resetInteractionMode(message: ResetInteractionMode) {
    }

    override fun playerInfo(message: PlayerInfo) {
        stateTracker.clearTempMoveSpeeds()
        // Assign the coord and name of each player that is being added
        preloadPlayerInfo(message)

        for ((_, update) in message.updates) {
            when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> {
                    processPlayerExtendedInfo(update.extendedInfo)
                }
                is PlayerUpdateType.HighResolutionIdle -> {
                    processPlayerExtendedInfo(update.extendedInfo)
                }
                is PlayerUpdateType.HighResolutionMovement -> {
                    processPlayerExtendedInfo(update.extendedInfo)
                }
                else -> {
                }
            }
        }

        // Update the last known coord and name of each player being processed
        postPlayerInfo(message)
    }

    private fun preloadPlayerInfo(message: PlayerInfo) {
        for ((index, update) in message.updates) {
            when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, update.coord))
                    preprocessExtendedInfo(index, update.extendedInfo)
                }
                is PlayerUpdateType.HighResolutionIdle -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    val player = stateTracker.getPlayer(index)
                    stateTracker.overridePlayer(Player(index, name, player.coord))
                    preprocessExtendedInfo(index, update.extendedInfo)
                }
                is PlayerUpdateType.HighResolutionMovement -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    val player = stateTracker.getPlayer(index)
                    stateTracker.overridePlayer(Player(index, name, player.coord))
                    preprocessExtendedInfo(index, update.extendedInfo)
                }
                else -> {
                    // No-op, no info to preload
                }
            }
        }
    }

    private fun preprocessExtendedInfo(
        index: Int,
        extendedInfo: List<ExtendedInfo>,
    ) {
        val moveSpeed = extendedInfo.firstOfInstanceOfNull<MoveSpeedExtendedInfo>()
        if (moveSpeed != null) {
            stateTracker.setCachedMoveSpeed(index, moveSpeed.speed)
        }
        val tempMoveSpeed = extendedInfo.firstOfInstanceOfNull<TemporaryMoveSpeedExtendedInfo>()
        if (tempMoveSpeed != null) {
            stateTracker.setTempMoveSpeed(index, tempMoveSpeed.speed)
        }
        if (index == stateTracker.localPlayerIndex) {
            val appearance = extendedInfo.firstOfInstanceOfNull<AppearanceExtendedInfo>()
            if (appearance != null) {
                monitor.onNameUpdate(appearance.name)
            }
        }
    }

    private fun processPlayerExtendedInfo(infos: List<ExtendedInfo>) {
        for (info in infos) {
            when (info) {
                is ChatExtendedInfo -> {
                }
                is FaceAngleExtendedInfo -> {
                }
                is MoveSpeedExtendedInfo -> {
                }
                is TemporaryMoveSpeedExtendedInfo -> {
                }
                is NameExtrasExtendedInfo -> {
                }
                is SayExtendedInfo -> {
                }
                is SequenceExtendedInfo -> {
                    binaryIndex.increment(IndexedType.SEQ, info.id)
                }
                is ExactMoveExtendedInfo -> {
                }
                is HitExtendedInfo -> {
                }
                is TintingExtendedInfo -> {
                }
                is SpotanimExtendedInfo -> {
                    for (spotanim in info.spotanims.values) {
                        appendCheckedSpotanim(spotanim.id)
                    }
                }
                is FacePathingEntityExtendedInfo -> {
                }
                is AppearanceExtendedInfo -> {
                    appendCheckedSeq(info.runAnim)
                    appendCheckedSeq(info.readyAnim)
                    appendCheckedSeq(info.turnAnim)
                    appendCheckedSeq(info.walkAnim)
                    appendCheckedSeq(info.walkAnimBack)
                    appendCheckedSeq(info.walkAnimLeft)
                    appendCheckedSeq(info.walkAnimRight)
                }
                else -> error("Unknown extended info: $info")
            }
        }
    }

    private fun appendCheckedSeq(id: Int?) {
        if (id == null || id == -1 || id == 0xFFFF) {
            return
        }
        binaryIndex.increment(IndexedType.SEQ, id)
    }

    private fun appendCheckedSpotanim(id: Int?) {
        if (id == null || id == -1 || id == 0xFFFF) {
            return
        }
        binaryIndex.increment(IndexedType.SPOTANIM, id)
    }

    private fun postPlayerInfo(message: PlayerInfo) {
        for ((index, update) in message.updates) {
            when (update) {
                is PlayerUpdateType.LowResolutionToHighResolution -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, update.coord))
                }
                is PlayerUpdateType.HighResolutionIdle -> {
                    val oldPlayer = stateTracker.getPlayerOrNull(index) ?: return
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, oldPlayer.coord))
                }
                is PlayerUpdateType.HighResolutionMovement -> {
                    val name = loadPlayerName(index, update.extendedInfo)
                    stateTracker.overridePlayer(Player(index, name, update.coord))
                }
                else -> {
                    // No-op, no info to preload
                }
            }
        }
    }

    private inline fun <reified S> List<*>.firstOfInstanceOfNull(): S? {
        return firstOrNull { it is S } as? S
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
            ?: stateTracker.getLastKnownPlayerName(index)
            ?: "null"
    }

    override fun npcInfoV5(message: NpcInfo) {
        val world = stateTracker.getActiveWorld()
        prenpcinfo(message)
        for ((index, update) in message.updates) {
            when (update) {
                is NpcUpdateType.Active -> {
                    processNpcExtendedInfo(update.extendedInfo)
                }
                NpcUpdateType.HighResolutionToLowResolution -> {
                }
                is NpcUpdateType.LowResolutionToHighResolution -> {
                    processNpcExtendedInfo(update.extendedInfo)
                    // Only log the amount of times a npc was added to high res
                    // Other information isn't very useful and could add bloat to the indexing
                    val npc = world.getNpcOrNull(index) ?: continue
                    binaryIndex.increment(IndexedType.NPC, npc.id)
                }
                NpcUpdateType.Idle -> {
                    // noop
                }
            }
        }
        postnpcinfo(message)
    }

    private fun prenpcinfo(message: NpcInfo) {
        val world = stateTracker.getActiveWorld()
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

    private fun postnpcinfo(message: NpcInfo) {
        val world = stateTracker.getActiveWorld()
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

    private fun processNpcExtendedInfo(infos: List<ExtendedInfo>) {
        for (info in infos) {
            when (info) {
                is ExactMoveExtendedInfo -> {
                }
                is FacePathingEntityExtendedInfo -> {
                }
                is HitExtendedInfo -> {
                }
                is SayExtendedInfo -> {
                }
                is SequenceExtendedInfo -> {
                    binaryIndex.increment(IndexedType.SEQ, info.id)
                }
                is TintingExtendedInfo -> {
                }
                is SpotanimExtendedInfo -> {
                    for (spotanim in info.spotanims.values) {
                        appendCheckedSpotanim(spotanim.id)
                    }
                }
                is OldSpotanimExtendedInfo -> {
                    appendCheckedSpotanim(info.id)
                }
                is BaseAnimationSetExtendedInfo -> {
                    appendCheckedSeq(info.turnLeftAnim)
                    appendCheckedSeq(info.turnRightAnim)
                    appendCheckedSeq(info.walkAnim)
                    appendCheckedSeq(info.walkAnimBack)
                    appendCheckedSeq(info.walkAnimLeft)
                    appendCheckedSeq(info.walkAnimRight)
                    appendCheckedSeq(info.runAnim)
                    appendCheckedSeq(info.runAnimBack)
                    appendCheckedSeq(info.runAnimLeft)
                    appendCheckedSeq(info.runAnimRight)
                    appendCheckedSeq(info.crawlAnim)
                    appendCheckedSeq(info.crawlAnimBack)
                    appendCheckedSeq(info.crawlAnimLeft)
                    appendCheckedSeq(info.crawlAnimRight)
                    appendCheckedSeq(info.readyAnim)
                }
                is BodyCustomisationExtendedInfo -> {
                }
                is HeadCustomisationExtendedInfo -> {
                }
                is CombatLevelChangeExtendedInfo -> {
                }
                is EnabledOpsExtendedInfo -> {
                }
                is FaceCoordExtendedInfo -> {
                }
                is NameChangeExtendedInfo -> {
                }
                is TransformationExtendedInfo -> {
                    binaryIndex.increment(IndexedType.NPC, info.id)
                }
                is HeadIconCustomisationExtendedInfo -> {
                }
            }
        }
    }
}
