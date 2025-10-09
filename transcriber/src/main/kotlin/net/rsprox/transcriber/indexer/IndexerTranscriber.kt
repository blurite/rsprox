package net.rsprox.transcriber.indexer

import net.rsprot.protocol.util.CombinedId
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.protocol.game.incoming.model.buttons.*
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsAddBannedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.AffinedClanSettingsSetMutedFromChannel
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelFullRequest
import net.rsprox.protocol.game.incoming.model.clan.ClanChannelKickUser
import net.rsprox.protocol.game.incoming.model.clan.ClanSettingsFullRequest
import net.rsprox.protocol.game.incoming.model.events.*
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatJoinLeave
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatKick
import net.rsprox.protocol.game.incoming.model.friendchat.FriendChatSetRank
import net.rsprox.protocol.game.incoming.model.locs.OpLoc
import net.rsprox.protocol.game.incoming.model.locs.OpLoc6
import net.rsprox.protocol.game.incoming.model.locs.OpLocT
import net.rsprox.protocol.game.incoming.model.messaging.MessagePrivate
import net.rsprox.protocol.game.incoming.model.messaging.MessagePublic
import net.rsprox.protocol.game.incoming.model.misc.client.*
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
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntity6
import net.rsprox.protocol.game.incoming.model.worldentities.OpWorldEntityT
import net.rsprox.protocol.game.outgoing.model.camera.*
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
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.MoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.NameExtrasExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo.TemporaryMoveSpeedExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.EnabledOpsExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExactMoveExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FaceAngleExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FacePathingEntityExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.HitExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SayExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SequenceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.TintingExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.worldentityinfo.*
import net.rsprox.protocol.game.outgoing.model.interfaces.*
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.game.outgoing.model.logout.Logout
import net.rsprox.protocol.game.outgoing.model.logout.LogoutTransfer
import net.rsprox.protocol.game.outgoing.model.logout.LogoutWithReason
import net.rsprox.protocol.game.outgoing.model.map.*
import net.rsprox.protocol.game.outgoing.model.misc.client.*
import net.rsprox.protocol.game.outgoing.model.misc.player.AccountFlags
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
import net.rsprox.protocol.game.outgoing.model.specific.*
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownString
import net.rsprox.protocol.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.game.outgoing.model.varp.VarpReset
import net.rsprox.protocol.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.game.outgoing.model.varp.VarpSync
import net.rsprox.protocol.game.outgoing.model.worldentity.ClearEntities
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorldV1
import net.rsprox.protocol.game.outgoing.model.worldentity.SetActiveWorldV2
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.game.outgoing.model.zone.payload.*
import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.indexing.BinaryIndex
import net.rsprox.shared.indexing.IndexedType
import net.rsprox.shared.indexing.increment
import net.rsprox.transcriber.Transcriber
import net.rsprox.transcriber.state.Npc
import net.rsprox.transcriber.state.SessionState

@Suppress("DuplicatedCode")
public class IndexerTranscriber(
    private val sessionState: SessionState,
    cacheProvider: CacheProvider,
    private val binaryIndex: BinaryIndex,
) : Transcriber {
    private fun getNpcInAnyWorld(index: Int): Npc? {
        for (world in sessionState.getAllWorlds()) {
            val npc = world.getNpcOrNull(index)
            if (npc != null) {
                return npc
            }
        }
        return null
    }

    override val cache: Cache = cacheProvider.get()

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

    override fun ifButtonX(message: If3Button) {
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

    override fun ifRunScript(message: IfRunScript) {
        incrementInterfaceId(message.interfaceId)
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

    override fun eventMouseClickV1(message: EventMouseClickV1) {
    }

    override fun eventMouseClickV2(message: EventMouseClickV2) {
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

    override fun opWorldEntity(message: OpWorldEntity) {
    }

    override fun opWorldEntity6(message: OpWorldEntity6) {
    }

    override fun opWorldEntityT(message: OpWorldEntityT) {
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

    override fun rsevenStatus(message: RSevenStatus) {
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

    override fun camTargetV3(message: CamTargetV3) {
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

    override fun worldEntityInfoV1(message: WorldEntityInfoV1) {
    }

    override fun worldEntityInfoV2(message: WorldEntityInfoV2) {
    }

    override fun worldEntityInfoV3(message: WorldEntityInfoV3) {
    }

    override fun worldEntityInfoV4(message: WorldEntityInfoV4) {
    }

    override fun worldEntityInfoV5(message: WorldEntityInfoV5) {
    }

    override fun worldEntityInfoV6(message: WorldEntityInfoV6) {
    }

    override fun ifClearInv(message: IfClearInv) {
        incrementInterfaceId(message.interfaceId)
    }

    override fun ifCloseSub(message: IfCloseSub) {
        incrementComponent(message.combinedId)
    }

    override fun ifMoveSub(message: IfMoveSub) {
        incrementComponent(message.sourceCombinedId)
        incrementComponent(message.destinationCombinedId)
    }

    override fun ifOpenSub(message: IfOpenSub) {
        incrementInterfaceId(message.interfaceId)
        incrementComponent(message.destinationCombinedId)
    }

    override fun ifOpenTop(message: IfOpenTop) {
        incrementInterfaceId(message.interfaceId)
    }

    override fun ifResyncV1(message: IfResyncV1) {
    }

    override fun ifResyncV2(message: IfResyncV2) {
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

    override fun ifSetEventsV1(message: IfSetEventsV1) {
        incrementComponent(message.combinedId)
    }

    override fun ifSetEventsV2(message: IfSetEventsV2) {
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
    }

    override fun rebuildLogin(message: RebuildLogin) {
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

    override fun rebuildWorldEntityV2(message: RebuildWorldEntityV2) {
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

    override fun rebuildWorldEntityV3(message: RebuildWorldEntityV3) {
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

    override fun packetGroupStart(message: PacketGroupStart) {
    }

    override fun packetGroupEnd(message: PacketGroupEnd) {
    }

    override fun zbuf(message: ZBuf) {
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

    override fun accountFlags(message: AccountFlags) {
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

    override fun projAnimSpecificV4(message: ProjAnimSpecificV4) {
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
        return sessionState.getAssociatedVarbits(basevar).filter { type ->
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
        val oldValue = sessionState.getVarp(id)
        val impactedVarbits = getImpactedVarbits(id, oldValue, newValue)
        // Ignore any varbits and varps set on tick 0
        if (sessionState.cycle == sessionState.lastConnection) {
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
    }

    override fun setActiveWorldV1(message: SetActiveWorldV1) {
    }

    override fun setActiveWorldV2(message: SetActiveWorldV2) {
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
    }

    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        for (update in message.packets) {
            when (update) {
                is LocAddChangeV1 -> {
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
                is MapProjAnimV1 -> {
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
    }

    override fun locAddChangeV1(message: LocAddChangeV1) {
        binaryIndex.increment(IndexedType.LOC, message.id)
    }

    override fun locAddChangeV2(message: LocAddChangeV2) {
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

    override fun mapProjAnimV1(message: MapProjAnimV1) {
        appendCheckedSpotanim(message.id)
    }

    override fun mapProjAnimV2(message: MapProjAnimV2) {
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

    override fun objCustomise(message: ObjCustomise) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objUncustomise(message: ObjUncustomise) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objAddSpecific(message: ObjAddSpecific) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objCountSpecific(message: ObjCountSpecific) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objDelSpecific(message: ObjDelSpecific) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objEnabledOpsSpecific(message: ObjEnabledOpsSpecific) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objCustomiseSpecific(message: ObjCustomiseSpecific) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun objUncustomiseSpecific(message: ObjUncustomiseSpecific) {
        binaryIndex.increment(IndexedType.OBJ, message.id)
    }

    override fun soundArea(message: SoundArea) {
        binaryIndex.increment(IndexedType.SYNTH, message.id)
    }

    override fun unknownString(message: UnknownString) {
    }

    override fun setInteractionMode(message: SetInteractionMode) {
    }

    override fun resetInteractionMode(message: ResetInteractionMode) {
    }

    override fun playerInfo(message: PlayerInfo) {
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

    override fun npcInfoV5(message: NpcInfo) {
        val world = sessionState.getActiveWorld()
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
                is FaceAngleExtendedInfo -> {
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
