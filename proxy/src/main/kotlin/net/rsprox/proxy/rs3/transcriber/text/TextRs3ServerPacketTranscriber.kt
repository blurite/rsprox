package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.cache.api.rs3.Rs3VariableDomain

import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivate
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPrivateEcho
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatClanchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatFriendchat
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageQuickchatPlayerGroup
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.Varclan

import net.rsprox.protocol.rs3.game.outgoing.model.appearance.LobbyAppearance
import net.rsprox.protocol.rs3.game.outgoing.model.appearance.PlayerSnapshot

import java.text.DecimalFormat
import java.text.NumberFormat
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
import net.rsprox.protocol.game.outgoing.model.misc.client.SiteSettings
import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownServerPacket
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateAccountReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateCheckEmailReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateCheckNameReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateSuggestNameError
import net.rsprox.protocol.rs3.game.outgoing.model.account.CreateSuggestNameReply
import net.rsprox.protocol.rs3.game.outgoing.model.account.FriendlistLoaded
import net.rsprox.protocol.rs3.game.outgoing.model.account.UpdateDob
import net.rsprox.protocol.rs3.game.outgoing.model.camera.*
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanChannelDelta
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanChannelFull
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanSettingsDelta
import net.rsprox.protocol.rs3.game.outgoing.model.clan.ClanSettingsFull
import net.rsprox.protocol.rs3.game.outgoing.model.debug.DbFilterDebug
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupDelta
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupFull
import net.rsprox.protocol.rs3.game.outgoing.model.group.PlayerGroupVarps
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.*
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.rs3.game.outgoing.model.map.EnvironmentOverride
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3.game.outgoing.model.map.Reconnect
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildRegion
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightAttenuationFalloff
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightColour
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightEnabled
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightExtendAbove
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightExtendBelow
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightIntensityScale
import net.rsprox.protocol.rs3.game.outgoing.model.map.lighting.PointLightShadow
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ChangeLobby
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ConsoleFeedback
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Cutscene2dPlay
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.DebugServerTriggers
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.DoCheat
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ExecuteClientCheat
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintTrail
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Js5Reload
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Logout
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.LogoutFull
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.LogoutTransfer
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.NoTimeout
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ResetAnims
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SendPing
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SetDrawOrder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ShowFaceHere
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.StoreReset
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.StoreServerpermVarcsAck
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.SyncClock
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.TickEnd
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.TriggerOnDialogAbort
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Unnamed1
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Unnamed2
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.UpdateRebootTimer
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.UpdateUid192
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.*
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.ClearPlayerSnapshot
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetMapFlag
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetMoveAction
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.UpdateStockmarketSlotV2
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectAdd
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectClear
import net.rsprox.protocol.rs3.game.outgoing.model.selection.LocSelectConfigure
import net.rsprox.protocol.rs3.game.outgoing.model.selection.SetLocOpOverride
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageClanchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageClanchannelSystem
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessageFriendchannel
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePlayerGroup
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivate
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePrivateEcho
import net.rsprox.protocol.rs3.game.outgoing.model.social.MessagePublic
import net.rsprox.protocol.rs3.game.outgoing.model.social.SocialNetworkLogout
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendchatChannelFull
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendchatChannelSingleUser
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendlist
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateIgnorelist
import net.rsprox.protocol.rs3.game.outgoing.model.social.UrlOpen
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiJingle
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiSong
import net.rsprox.protocol.rs3.game.outgoing.model.sound.MidiSongStop
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SongPreload
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundMixbussAdd
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundMixbussSetLevel
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SoundStop
import net.rsprox.protocol.rs3.game.outgoing.model.sound.SynthSound
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisPreloadSounds
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSound
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroup
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroupStart
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSoundGroupStop
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSpeechSound
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSpeechStop
import net.rsprox.protocol.rs3.game.outgoing.model.specific.LocAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcHeadiconSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.NpcSaySpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.PlayerAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.specific.SpotanimSpecific
import net.rsprox.protocol.rs3.game.outgoing.model.specific.SpotanimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryClearGridValue
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddColumn
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddGroup
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridAddRow
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridFull
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveColumn
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridMoveRow
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveColumn
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveGroup
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridRemoveRow
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridSetRowPinned
import net.rsprox.protocol.rs3.game.outgoing.model.telemetry.TelemetryGridValuesDelta
import net.rsprox.protocol.rs3.game.outgoing.model.unknown.RawUnknownServerPacket
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.Varbit
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitLarge
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitSmall
import net.rsprox.protocol.rs3.game.outgoing.model.varc.*
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.VarclanDisable
import net.rsprox.protocol.rs3.game.outgoing.model.varclan.VarclanEnable
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.*
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ServerPacketTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.any
import net.rsprox.shared.property.filteredAny
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.coordGridProperty
import net.rsprox.shared.property.enum
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.filteredScriptVarType
import net.rsprox.shared.property.formattedInt
import net.rsprox.shared.property.formattedLong
import net.rsprox.shared.property.group
import net.rsprox.shared.property.int
import net.rsprox.shared.property.list
import net.rsprox.shared.property.long
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.regular.AnyProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.script
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.property.varbit
import net.rsprox.shared.property.varc
import net.rsprox.shared.property.varobj
import net.rsprox.shared.property.varp
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.protocol.rs3v949.game.outgoing.model.camera.CamForceAngle as LegacyCamForceAngle
import net.rsprox.protocol.rs3v949.game.outgoing.model.camera.CamShake as LegacyCamShake
import net.rsprox.protocol.rs3v949.game.outgoing.model.camera.CameraUpdate as LegacyCameraUpdate
import net.rsprox.protocol.rs3v949.game.outgoing.model.map.RebuildNormal as LegacyRebuildNormal
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.client.HintArrow as LegacyHintArrow
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.client.HintTrail as LegacyHintTrail
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.MessageGame as LegacyMessageGame
import net.rsprox.protocol.rs3v949.game.outgoing.model.misc.player.SetPlayerOp as LegacySetPlayerOp
import net.rsprox.protocol.rs3v949.game.outgoing.model.specific.ProjAnimSpecificV2 as LegacyProjAnimSpecificV2
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapProjAnim as LegacyMapProjAnim
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapProjAnimHalfsq as LegacyMapProjAnimHalfsq
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapProjAnimHalfsqV2 as LegacyMapProjAnimHalfsqV2
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MapProjAnimV2 as LegacyMapProjAnimV2
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.MidiSongLocation as LegacyMidiSongLocation
import net.rsprox.protocol.rs3v949.game.outgoing.model.zone.payload.SoundArea as LegacySoundArea

public class TextRs3ServerPacketTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : Rs3ServerPacketTranscriber {
    private val coordinates = Rs3CoordinateProperties(sessionState, settingSetStore)
    private val entities = Rs3EntityProperties(sessionState, settingSetStore)

    private fun Property.coordGrid(
        name: String,
        coord: CoordGrid,
    ): ScriptVarTypeProperty<*> = coordinates.append(this, name, coord)

    private fun Property.coordGrid(
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> = coordinates.append(this, level, x, z, name)

    private val root: RootProperty
        get() = checkNotNull(sessionState.root.lastOrNull()) {
            "No active root - onTranscribeStart() must run before dispatching to a transcriber method"
        }
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    private fun Property.zoneCoord(x: Int, z: Int, name: String = "coord") {
        val coord = sessionState.getActiveWorld().relativizeZoneCoord(x, z)
        if (coord != CoordGrid.INVALID) {
            coordGrid(name, coord)
        } else {
            group(name) {
                int("xinzone", x)
                int("zinzone", z)
                any("origin", "unknown")
            }
        }
    }

    private fun Property.zoneHalfCoord(x: Int, z: Int, name: String = "coord") {
        val origin = sessionState.getActiveWorld().relativizeZoneCoord(0, 0)
        if (origin != CoordGrid.INVALID) {
            coordinates.appendFine(
                this,
                origin.level,
                origin.x + (x shr 1),
                origin.z + (z shr 1),
                (x and 1) * 64,
                (z and 1) * 64,
                name,
            )
        } else {
            group(name) {
                int("halfxinzone", x)
                int("halfzinzone", z)
                any("origin", "unknown")
            }
        }
    }

    override fun messageQuickchatPrivateServer(message: MessageQuickchatPrivate) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE]) return omit()
        root.string("from", message.sender)
        message.alternateSender?.let { root.string("alternate", it) }
        root.int("alternateflag", message.alternateSenderFlag)
        root.int("world", message.messageWorld)
        root.int("counter", message.messageCounter)
        root.int("playertype", message.playerType)
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun messageQuickchatPrivateEcho(message: MessageQuickchatPrivateEcho) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE]) return omit()
        root.string("to", message.recipient)
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun messageQuickchatClanchannel(message: MessageQuickchatClanchannel) {
        if (!filters[PropertyFilter.MESSAGE_CLANCHANNEL]) return omit()
        root.int("channel", message.channel)
        root.string("from", message.sender)
        root.int("world", message.messageWorld)
        root.int("counter", message.messageCounter)
        root.int("playertype", message.playerType)
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun messageQuickchatFriendchat(message: MessageQuickchatFriendchat) {
        if (!filters[PropertyFilter.MESSAGE_FRIENDCHANNEL]) return omit()
        root.string("from", message.sender)
        message.alternateSender?.let { root.string("alternate", it) }
        root.int("alternateflag", message.alternateSenderFlag)
        root.string("channel", message.channelName)
        root.int("world", message.messageWorld)
        root.int("counter", message.messageCounter)
        root.int("playertype", message.playerType)
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun messageQuickchatPlayerGroup(message: MessageQuickchatPlayerGroup) {
        if (!filters[PropertyFilter.GROUP]) return omit()
        root.string("from", message.sender)
        root.int("broadcast", message.broadcast)
        root.int("world", message.messageWorld)
        root.int("counter", message.messageCounter)
        root.int("playertype", message.playerType)
        root.appendQuickChat(message.phraseId, message.quickChat)
    }

    override fun varclan(message: Varclan) {
        if (!filters[PropertyFilter.VARCLAN]) return omit()
        root.appendVariable(message.variable)
    }

    override fun lobbyAppearance(message: LobbyAppearance) {
        root.int("flags", message.appearanceFlags)
        root.appendAppearanceBody(message.appearance)
    }

    override fun playerSnapshot(message: PlayerSnapshot) {
        root.int("slot", message.snapshotIndex)
        root.int("flags", message.appearanceFlags)
        root.appendAppearanceBody(message.appearance)
    }

    override fun lastLoginInfo(message: LastLoginInfo) {
        if (!filters[PropertyFilter.LAST_LOGIN_INFO]) return omit()
        root.int("lastlogin", message.lastLogin)
    }

    override fun updateRebootTimer(message: UpdateRebootTimer) {
        if (!filters[PropertyFilter.UPDATE_REBOOT_TIMER]) return omit()
        root.int("duration", message.duration)
    }

    override fun chatFilterSettings(message: ChatFilterSettings) {
        if (!filters[PropertyFilter.CHAT_FILTER_SETTINGS]) return omit()
        root.int("filterslot1", message.filterSlot1)
        root.int("filterslot0", message.filterSlot0)
    }

    override fun logoutFull(message: LogoutFull) {
        if (!filters[PropertyFilter.LOGOUT]) return omit()
        root.int("reason", message.reason)
    }

    override fun camReset(message: CamReset) {
        if (!filters[PropertyFilter.CAM_RESET]) return omit()
    }

    override fun cam2Enable(message: Cam2Enable) {
        if (!filters[PropertyFilter.CAM_RESET]) return omit()
        root.boolean("enabled", message.enabled)
    }

    override fun camSmoothReset(message: CamSmoothReset) {
        if (!filters[PropertyFilter.CAM_RESET]) return omit()
    }

    override fun showFaceHere(message: ShowFaceHere) {
        if (!filters[PropertyFilter.SHOW_FACE_HERE]) return omit()
        root.boolean("enabled", message.enabled)
    }

    override fun setDrawOrder(message: SetDrawOrder) {
        if (!filters[PropertyFilter.SETDRAWORDER]) return omit()
        root.int("order", message.order)
    }

    override fun triggerOnDialogAbort(message: TriggerOnDialogAbort) {
        if (!filters[PropertyFilter.TRIGGER_ONDIALOGABORT]) return omit()
    }

    override fun synthSound(message: SynthSound) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("id", ScriptVarType.SYNTH, message.id)
        root.filteredInt("loops", message.loops, 1)
        root.filteredInt("delay", message.delay, 0)
        root.int("volume", message.volume)
        root.int("rate", message.rate)
    }

    override fun vorbisSpeechSound(message: VorbisSpeechSound) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("id", ScriptVarType.SYNTH, message.id)
        root.int("loops", message.loops)
        root.int("delay", message.delay)
        root.int("volume", message.volume)
    }

    override fun vorbisSoundGroup(message: VorbisSoundGroup) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("id", ScriptVarType.SYNTH, message.id)
        root.int("loops", message.loops)
        root.int("delay", message.delay)
        root.int("volume", message.volume)
        root.int("rate", message.rate)
        root.int("group", message.group)
    }

    override fun vorbisPreloadSounds(message: VorbisPreloadSounds) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("id", ScriptVarType.SYNTH, message.id)
    }

    override fun soundMixbussAdd(message: SoundMixbussAdd) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.int("bus", message.bus)
        root.int("parent", message.parent)
        root.int("level", message.level)
    }

    override fun vorbisSoundGroupStart(message: VorbisSoundGroupStart) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.int("group", message.group)
    }

    override fun vorbisSoundGroupStop(message: VorbisSoundGroupStop) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.int("group", message.group)
    }

    override fun songPreload(message: SongPreload) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.scriptVarType("id", ScriptVarType.MIDI, message.id)
    }

    override fun midiSong(message: MidiSong) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.scriptVarType("id", ScriptVarType.MIDI, message.id)
        root.int("volume", message.volume)
    }

    override fun soundMixbussSetLevel(message: SoundMixbussSetLevel) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.int("bus", message.bus)
        root.int("level", message.level)
    }

    override fun soundStop(message: SoundStop) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("id", ScriptVarType.SYNTH, message.id)
        root.int("group", message.group)
    }

    override fun midiJingle(message: MidiJingle) {
        if (!filters[PropertyFilter.MIDI_JINGLE]) return omit()
        root.scriptVarType("id", ScriptVarType.MIDI, message.song)
        root.int("volume", message.volume)
    }

    override fun midiSongStop(message: MidiSongStop) {
        if (!filters[PropertyFilter.MIDI_SONG_STOP]) return omit()
    }

    override fun vorbisSpeechStop(message: VorbisSpeechStop) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
    }

    private fun hex(bytes: ByteArray): String = bytes.joinToString(" ") { "%02x".format(it) }

    private companion object {
        private val KG_NUMBER_FORMAT: NumberFormat = DecimalFormat("###,###,###kg")
    }

    override fun varbit(message: Varbit) {
        if (!filters[PropertyFilter.VARBITS]) return omit()
        root.varbit("varbit", message.id)
        root.int("value", message.value)
    }

    override fun varcBit(message: VarcBit) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.int("varcbit", message.id)
        root.int("value", message.value)
    }

    override fun resetClientVarcache(message: ResetClientVarcache) {
        if (!filters[PropertyFilter.VARC]) return omit()
    }

    override fun varcLong(message: VarcLong) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.varc("varc", message.id)
        root.long("value", message.value)
    }

    override fun varcStrLarge(message: VarcStrLarge) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.varc("varc", message.id)
        root.string("value", message.value)
    }

    override fun varpSmall(message: VarpSmall) {
        if (!filters[PropertyFilter.VARP]) return omit()
        root.varp("varp", message.id)
        root.int("value", message.value)
    }

    override fun varpLarge(message: VarpLarge) {
        if (!filters[PropertyFilter.VARP]) return omit()
        root.varp("varp", message.id)
        root.int("value", message.value)
    }

    override fun varpLong(message: VarpLong) {
        if (!filters[PropertyFilter.VARP]) return omit()
        root.varp("varp", message.id)
        root.long("value", message.value)
    }

    override fun varbitSmall(message: VarbitSmall) {
        if (!filters[PropertyFilter.VARBITS]) return omit()
        root.varbit("varbit", message.id)
        root.int("value", message.value)
    }

    override fun varbitLarge(message: VarbitLarge) {
        if (!filters[PropertyFilter.VARBITS]) return omit()
        root.varbit("varbit", message.id)
        root.int("value", message.value)
    }

    override fun ifOpenSubActivePlayer(message: IfOpenSubActivePlayer) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.component("com", message.componentHash)
        root.int("playerindex", message.playerIndex)
        root.scriptVarType("id", ScriptVarType.INTERFACE, message.childId)
        root.ifType(message.layer)
        root.xteas(message.legacyWord0, message.legacyWord1, message.legacyWord2, message.legacyWord3)
    }

    override fun ifOpenSubActiveNpc(message: IfOpenSubActiveNpc) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.component("com", message.componentHash)
        root.int("npcindex", message.npcIndex)
        root.scriptVarType("id", ScriptVarType.INTERFACE, message.childId)
        root.ifType(message.layer)
        root.xteas(message.legacyWord0, message.legacyWord1, message.legacyWord2, message.legacyWord3)
    }

    override fun ifOpenTop(message: IfOpenTop) {
        if (!filters[PropertyFilter.IF_OPENTOP]) return omit()
        val existing = sessionState.toplevelInterface
        root.filteredScriptVarType("previousid", ScriptVarType.INTERFACE, existing, -1)
        root.scriptVarType("id", ScriptVarType.INTERFACE, message.interfaceId)
        message.unused?.let { root.int("unused", it) }
        root.xteas(message.legacyWord0, message.legacyWord1, message.legacyWord2, message.legacyWord3)
    }

    override fun ifOpenSub(message: IfOpenSub) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.INTERFACE, message.childId)
        root.ifType(message.layer)
        root.xteas(message.legacyWord0, message.legacyWord1, message.legacyWord2, message.legacyWord3)
    }

    override fun ifCloseSub(message: IfCloseSub) {
        if (!filters[PropertyFilter.IF_CLOSESUB]) return omit()
        root.component("com", message.parentComponentHash)
        val interfaceId = sessionState.getOpenInterface(message.parentComponentHash)
        if (interfaceId != null) {
            root.scriptVarType("id", ScriptVarType.INTERFACE, interfaceId)
        }
    }

    override fun ifSetHide(message: IfSetHide) {
        if (!filters[PropertyFilter.IF_SETHIDE]) return omit()
        root.component("com", message.componentHash)
        root.boolean("hide", message.hidden)
    }

    override fun messagePublic(message: MessagePublic) {
        if (!filters[PropertyFilter.MESSAGE_PUBLIC_SERVER]) return omit()
        root.int("playerindex", message.playerIndex)
        root.int("coloureffectandquickflag", message.colourEffectAndQuickFlag)
        root.int("playertype", message.playerType)
        message.message?.let { root.string("message", it) }
        message.quickChat?.let { root.appendQuickChat(checkNotNull(message.phraseId), it) }
    }

    override fun playerGroupFull(message: PlayerGroupFull) {
        if (!filters[PropertyFilter.GROUP]) return omit()
        message.version?.let { root.int("version", it) }
        message.group?.let { root.group("group") { appendPlayerGroup(it) } }
    }

    override fun playerGroupDelta(message: PlayerGroupDelta) {
        if (!filters[PropertyFilter.GROUP]) return omit()
        root.long("discardedkey", message.discardedKey)
        root.int("revision", message.revision)
        root.appendPlayerGroupRecords(message.records)
        root.int("terminator", message.terminator)
    }

    override fun playerGroupVarps(message: PlayerGroupVarps) {
        if (!filters[PropertyFilter.GROUP]) return omit()
        root.int("memberindex", message.memberIndex)
        root.int("clear", message.clear)
        root.appendVariables(message.variables, Rs3VariableDomain.PLAYER)
    }

    override fun messagePrivateEcho(message: MessagePrivateEcho) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE]) return omit()
        root.string("to", message.recipient)
        root.string("message", message.message)
    }

    override fun messagePrivate(message: MessagePrivate) {
        if (!filters[PropertyFilter.MESSAGE_PRIVATE]) return omit()
        root.int("alternatesenderflag", message.alternateSenderFlag)
        root.string("from", message.sender)
        message.alternateSender?.let { root.string("alternatesender", it) }
        root.int("world", message.messageWorld)
        root.int("mescount", message.messageCounter)
        root.int("chatcrown", message.playerType)
        root.string("message", message.message)
    }

    override fun messageClanchannel(message: MessageClanchannel) {
        if (!filters[PropertyFilter.MESSAGE_CLANCHANNEL]) return omit()
        root.int("clantype", message.channel)
        root.string("name", message.sender)
        root.int("world", message.messageWorld)
        root.int("mescount", message.messageCounter)
        root.int("chatcrown", message.playerType)
        root.string("message", message.message)
    }

    override fun messageFriendchannel(message: MessageFriendchannel) {
        if (!filters[PropertyFilter.MESSAGE_FRIENDCHANNEL]) return omit()
        root.int("alternatesenderflag", message.alternateSenderFlag)
        root.string("name", message.sender)
        message.alternateSender?.let { root.string("alternatesender", it) }
        root.string("channelname", message.channelName)
        root.int("world", message.messageWorld)
        root.int("mescount", message.messageCounter)
        root.int("chatcrown", message.playerType)
        root.string("message", message.message)
    }

    override fun messagePlayerGroup(message: MessagePlayerGroup) {
        if (!filters[PropertyFilter.GROUP]) return omit()
        root.string("sender", message.sender)
        root.int("messageworld", message.messageWorld)
        root.int("messagecounter", message.messageCounter)
        root.int("playertype", message.playerType)
        root.int("broadcast", message.broadcast)
        root.string("message", message.message)
    }

    override fun messageClanchannelSystem(message: MessageClanchannelSystem) {
        if (!filters[PropertyFilter.MESSAGE_CLANCHANNEL]) return omit()
        root.int("clantype", message.channel)
        root.int("world", message.messageWorld)
        root.int("mescount", message.messageCounter)
        root.string("message", message.message)
    }

    override fun dbFilterDebug(message: DbFilterDebug) {
        if (!filters[PropertyFilter.DBFILTER_DEBUG]) return omit()
        root.int("version", message.version)
        root.int("titlemarker", message.titleMarker)
        message.title?.let { root.string("title", it) }
        root.children += AnyProperty("filter", message.filter, DbFilterDebug.Filter::class.java)
    }

    override fun urlOpen(message: UrlOpen) {
        if (!filters[PropertyFilter.URL_OPEN]) return omit()
        root.int("mode", message.mode)
        root.string("url", message.url)
        message.preferredUrl?.let { root.string("preferredurl", it) }
    }

    override fun socialNetworkLogout(message: SocialNetworkLogout) {
        if (!filters[PropertyFilter.URL_OPEN]) return omit()
        root.string("url", message.url)
    }

    override fun siteSettings(message: SiteSettings) {
        if (!filters[PropertyFilter.SITE_SETTINGS]) return omit()
        root.string("settings", message.settings)
    }

    private fun Property.xteas(vararg keys: Int?) {
        if (keys.all { it == null || it == 0 }) return
        group("xteas") {
            keys.forEachIndexed { index, key -> any("key$index", key) }
        }
    }

    override fun worldlistFetchReply(message: WorldlistFetchReply) {
        if (!filters[PropertyFilter.WORLDLIST_FETCH_REPLY]) return omit()
        root.int("completeflag", message.completeFlag)
        root.int("chunklength", message.chunkLength)
        root.int("accumulatedlength", message.accumulatedLength)
        message.reply?.let { root.appendWorldList(it) }
    }

    override fun messageGame(message: MessageGame) {
        if (!filters[PropertyFilter.MESSAGE_GAME]) return omit()
        root.int("type", message.type)
        root.int("channel", message.channel)
        root.int("flags", message.flags)
        message.sender?.let { root.string("sender", it) }
        message.alternateSender?.let { root.string("alternatesender", it) }
        root.string("message", message.message)
    }

    override fun legacyMessageGame(message: LegacyMessageGame) {
        if (!filters[PropertyFilter.MESSAGE_GAME]) return omit()
        root.int("type", message.type)
        root.filteredInt("flags", message.effectFlags, 0)
        val name = message.name
        if (name != null) {
            root.string("name", name)
        }
        root.string("message", message.message)
    }

    override fun rebuildNormal(message: RebuildNormal) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        root.sceneBase(message.baseTileX, message.baseTileZ)
        root.int("basechunkx", message.baseChunkX)
        root.int("basechunkz", message.baseChunkZ)
        root.int("format", message.format)
        root.int("npcscenevalue", message.npcSceneValue)
        root.int("reserved", message.reserved)
        root.int("templateid", message.templateId)
        root.int("minimumcoordinate", message.minimumCoordinate)
        root.int("maximumcoordinate", message.maximumCoordinate)
    }

    override fun reconnect(message: Reconnect) {
        val init = message.playerInfoInit
        root.coordGrid(init.localPlayerLevel, init.localPlayerX, init.localPlayerZ, "localplayercoord")
    }

    override fun legacyRebuildNormal(message: LegacyRebuildNormal) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        val initBlock = message.playerInfoInitBlock
        root.filteredBoolean("gpitooshort", initBlock == null)
        if (initBlock != null) {
            root.coordGrid(
                initBlock.localPlayerLevel,
                initBlock.localPlayerX,
                initBlock.localPlayerZ,
                "self",
            )
            root.int("nonzeropositioncount", initBlock.nonZeroPositionCount)
        }
        root.filteredBoolean("trailermisaligned", !message.trailerAligned)
        if (!message.trailerAligned) {
            return
        }
        root.int("worldareatypeid", message.worldAreaTypeId)
        root.coordGridProperty(0, message.baseTileX, message.baseTileZ, "basetile")
    }

    private fun Property.buildZoneFollowsCommon(level: Int, zoneX: Int, zoneZ: Int) {
        val base = sessionState.getActiveWorld().relativizeZoneCoord(0, 0)
        if (base != CoordGrid.INVALID) {
            coordGrid("coord", base)
        } else {
            group("zone") {
                int("level", level)
                int("xoffset", zoneX)
                int("zoffset", zoneZ)
                any("origin", "unknown")
            }
        }
    }

    private fun Property.sceneBase(
        x: Int,
        z: Int,
    ) {
        if (x in 0..16383 && z in 0..16383) {
            coordGridProperty(0, x, z, "basetile")
        } else {
            group("basetile") {
                int("x", x)
                int("z", z)
            }
        }
    }

    override fun updateZoneFullFollows(message: UpdateZoneFullFollows) {
        if (!filters[PropertyFilter.ZONE_HEADER]) return omit()
        root.buildZoneFollowsCommon(message.level, message.zoneX, message.zoneZ)
    }

    override fun updateZonePartialFollows(message: UpdateZonePartialFollows) {
        if (!filters[PropertyFilter.ZONE_HEADER]) return omit()
        root.buildZoneFollowsCommon(message.level, message.zoneX, message.zoneZ)
    }

    private fun Property.buildLocPrefetch(event: LocPrefetch) {
        scriptVarType("id", ScriptVarType.LOC, event.id)
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, event.shape)
        event.rotation?.let { int("rotation", it) }
    }

    private fun Property.buildLocCustomise(event: LocCustomise) {
        scriptVarType("id", ScriptVarType.LOC, event.locId)
        zoneCoord(event.xInZone, event.zInZone)
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, event.shape)
        int("rotation", event.rotation)
        event.customisationFlags?.let {
            filteredBoolean("reset", it and 1 != 0)
            filteredInt("unknownflags", it and 0xF0, 0)
        }
        if (event.hasExtendedTransform) {
            // An absent transform retains existing state; do not emit a synthetic identity transform.
            locationTransform(
                event.rotationX,
                event.rotationY,
                event.rotationZ,
                event.rotationW,
                event.translationX,
                event.translationY,
                event.translationZ,
                event.scaleX,
                event.scaleY,
                event.scaleZ,
            )
        }
        event.models?.let { models ->
            group("MODELS") {
                int("count", models.size)
                models.forEachIndexed { slot, id ->
                    group {
                        int("slot", slot)
                        scriptVarType("id", ScriptVarType.MODEL, id)
                    }
                }
            }
        }
        event.recolours?.let { recolours ->
            group("RECOLOURS") {
                int("count", recolours.size)
                recolours.forEachIndexed { slot, colour ->
                    group {
                        int("slot", slot)
                        int("colour", colour)
                    }
                }
            }
        }
        event.retextures?.let { retextures ->
            group("RETEXTURES") {
                int("count", retextures.size)
                retextures.forEachIndexed { slot, texture ->
                    group {
                        int("slot", slot)
                        scriptVarType("texture", ScriptVarType.TEXTURE, texture)
                    }
                }
            }
        }
    }

    override fun locCustomise(message: LocCustomise) {
        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) return omit()
        root.buildLocCustomise(message)
    }

    private fun Property.buildLocAnim(event: LocAnim) {
        zoneCoord(event.xInZone, event.zInZone)
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, event.shape)
        int("rotation", event.rotation)
        scriptVarType("anim", ScriptVarType.SEQ, event.id)
        filteredInt("delay", event.delay, 0)
        if (event.hasExtendedTransform) {
            locationTransform(
                event.rotationX,
                event.rotationY,
                event.rotationZ,
                event.rotationW,
                event.translationX,
                event.translationY,
                event.translationZ,
                event.scaleX,
                event.scaleY,
                event.scaleZ,
            )
        }
    }

    override fun locAnim(message: LocAnim) {
        if (!filters[PropertyFilter.LOC_ANIM]) return omit()
        root.buildLocAnim(message)
    }

    private fun Property.buildLocAddChange(event: LocAddChange) {
        scriptVarType("id", ScriptVarType.LOC, event.locId)
        zoneCoord(event.xInZone, event.zInZone)
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, event.shape)
        int("rotation", event.rotation)
        if (event.hasExtendedTransform) {
            // An absent transform may retain existing state; only log an explicitly supplied transform.
            locationTransform(
                event.rotationX,
                event.rotationY,
                event.rotationZ,
                event.rotationW,
                event.translationX,
                event.translationY,
                event.translationZ,
                event.scaleX,
                event.scaleY,
                event.scaleZ,
            )
        }
    }

    private fun Property.locationTransform(
        rotationX: Float,
        rotationY: Float,
        rotationZ: Float,
        rotationW: Float,
        translationX: Float,
        translationY: Float,
        translationZ: Float,
        scaleX: Float,
        scaleY: Float,
        scaleZ: Float,
    ) {
        group("TRANSFORM") {
            group("ROTATION") {
                any("x", rotationX)
                any("y", rotationY)
                any("z", rotationZ)
                any("w", rotationW)
            }
            group("TRANSLATION") {
                any("x", translationX)
                any("y", translationY)
                any("z", translationZ)
            }
            group("SCALE") {
                any("x", scaleX)
                any("y", scaleY)
                any("z", scaleZ)
            }
        }
    }

    override fun locAddChange(message: LocAddChange) {
        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) return omit()
        root.buildLocAddChange(message)
    }

    private fun Property.buildLegacyMidiSongLocation(event: LegacyMidiSongLocation) {
        scriptVarType("song", ScriptVarType.MIDI, event.id)
        zoneCoord(event.xInZone, event.zInZone)
        int("maxdistance", event.maxDistance)
        int("mindistance", event.minDistance)
        int("volume", event.volume)
    }

    override fun midiSongLocation(message: MidiSongLocation) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.scriptVarType("song", ScriptVarType.MIDI, message.id)
        root.int("coordinate", message.coordinate)
        root.int("level", message.level)
        root.int("x", message.x)
        root.int("z", message.z)
        root.int("radius", message.radius)
        root.int("range", message.range)
        root.int("volume", message.volume)
    }

    override fun legacyMidiSongLocation(message: LegacyMidiSongLocation) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.buildLegacyMidiSongLocation(message)
    }

    private fun Property.buildLocDel(event: LocDel) {
        zoneCoord(event.xInZone, event.zInZone)
        scriptVarType("shape", ScriptVarType.LOC_SHAPE, event.shape)
        int("rotation", event.rotation)
        if (event.hasExtendedTransform) {
            locationTransform(
                event.rotationX,
                event.rotationY,
                event.rotationZ,
                event.rotationW,
                event.translationX,
                event.translationY,
                event.translationZ,
                event.scaleX,
                event.scaleY,
                event.scaleZ,
            )
        }
    }

    override fun locDel(message: LocDel) {
        if (!filters[PropertyFilter.LOC_DEL]) return omit()
        root.buildLocDel(message)
    }

    private fun Property.buildObjAdd(event: ObjAdd) {
        scriptVarType("id", ScriptVarType.OBJ, event.objId)
        formattedInt("count", event.count)
        zoneCoord(event.xInZone, event.zInZone)
    }

    override fun objAdd(message: ObjAdd) {
        if (!filters[PropertyFilter.OBJ_ADD]) return omit()
        root.buildObjAdd(message)
    }

    private fun Property.buildObjDel(event: ObjDel) {
        scriptVarType("id", ScriptVarType.OBJ, event.objId)
        zoneCoord(event.xInZone, event.zInZone)
    }

    override fun objDel(message: ObjDel) {
        if (!filters[PropertyFilter.OBJ_DEL]) return omit()
        root.buildObjDel(message)
    }

    private fun Property.buildObjCount(event: ObjCount) {
        scriptVarType("id", ScriptVarType.OBJ, event.objId)
        formattedInt("oldcount", event.oldQuantity)
        formattedInt("newcount", event.newQuantity)
        zoneCoord(event.xInZone, event.zInZone)
    }

    override fun objCount(message: ObjCount) {
        if (!filters[PropertyFilter.OBJ_COUNT]) return omit()
        root.buildObjCount(message)
    }

    private fun Property.buildObjReveal(event: ObjReveal) {
        scriptVarType("id", ScriptVarType.OBJ, event.objId)
        formattedInt("count", event.count)
        zoneCoord(event.xInZone, event.zInZone)
        group("EXCLUDED") {
            entities.player(this, event.excludedPlayerIndex)
        }
    }

    override fun objReveal(message: ObjReveal) {
        if (!filters[PropertyFilter.OBJ_ADD]) return omit()
        root.buildObjReveal(message)
    }

    private fun Property.buildMapAnim(event: MapAnim) {
        scriptVarType("id", ScriptVarType.SPOTANIM, event.id)
        filteredInt("delay", event.delay, 0)
        filteredInt("height", event.height, 0)
        zoneCoord(event.xInZone, event.zInZone)
        int("rotation", event.rotation)
        event.unused0?.let { int("unused0", it) }
        event.unused1?.let { int("unused1", it) }
        event.unused2?.let { int("unused2", it) }
    }

    override fun mapAnim(message: MapAnim) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildMapAnim(message)
    }

    private fun Property.buildMapAnimV1(event: MapAnimV1) {
        scriptVarType("id", ScriptVarType.SPOTANIM, if (event.id == 65535) -1 else event.id)
        filteredInt("delay", event.delay, 0)
        filteredInt("height", event.height, 0)
        filteredInt("rotation", event.rotation, 0)
        filteredBoolean("independentrotation", event.independentRotation)
        zoneCoord(event.xInZone, event.zInZone)
        int("unused0", event.unused0)
        int("unused1", event.unused1)
        int("unused2", event.unused2)
    }

    override fun mapAnimV1(message: MapAnimV1) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildMapAnimV1(message)
    }

    private fun Property.buildMapAnimV2(event: MapAnimV2) {
        scriptVarType("id", ScriptVarType.SPOTANIM, if (event.id == 65535) -1 else event.id)
        filteredInt("delay", event.delay, 0)
        filteredInt("height", event.height, 0)
        filteredInt("rotation", event.rotation, 0)
        filteredInt("offsetx", event.offsetX, 0)
        filteredInt("offsetz", event.offsetZ, 0)
        filteredBoolean("relativeoffset", event.relativeOffset)
        filteredBoolean("independentrotation", event.independentRotation)
        zoneCoord(event.xInZone, event.zInZone)
        event.unused0?.let { int("unused0", it) }
        event.unused1?.let { int("unused1", it) }
        event.unused2?.let { int("unused2", it) }
    }

    override fun mapAnimV2(message: MapAnimV2) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildMapAnimV2(message)
    }

    private fun Property.buildSoundAreaV1(event: SoundAreaV1) {
        scriptVarType("id", ScriptVarType.SYNTH, event.id)
        filteredInt("loops", event.loops, 0)
        filteredInt("delay", event.delay, 0)
        int("range", event.range)
        int("volume", event.volume)
        int("rate", event.rate)
        zoneCoord(event.xInZone, event.zInZone)
    }

    override fun soundAreaV1(message: SoundAreaV1) {
        if (!filters[PropertyFilter.SOUND_AREA]) return omit()
        root.buildSoundAreaV1(message)
    }
    private fun Property.buildSoundAreaV2(event: SoundAreaV2) {
        scriptVarType("id", ScriptVarType.SYNTH, event.id)
        filteredInt("loops", event.loops, 0)
        filteredInt("delay", event.delay, 0)
        int("range", event.range)
        int("volume", event.volume)
        int("rate", event.rate)
        boolean("speech", event.speech)
        zoneCoord(event.xInZone, event.zInZone)
    }

    override fun soundAreaV2(message: SoundAreaV2) {
        if (!filters[PropertyFilter.SOUND_AREA]) return omit()
        root.buildSoundAreaV2(message)
    }
    private fun Property.buildMapProjAnim(event: MapProjAnim) {
        scriptVarType("id", ScriptVarType.SPOTANIM, if (event.id == 65535) -1 else event.id)
        int("starttime", event.startTime)
        int("endtime", event.endTime)
        int("angle", event.angle)
        int("progress", event.progress)
        int("startheight", event.startHeight)
        int("endheight", event.endHeight)
        filteredBoolean("followterrain", event.followTerrain)
        filteredBoolean("unusedcoordbit", event.unusedCoordinateBit)
        group("SOURCE") {
            zoneCoord(event.xInZone, event.zInZone)
        }
        group("TARGET") {
            zoneCoord(event.xInZone + event.deltaX, event.zInZone + event.deltaZ)
            projectileActor(event.target)
        }
        int("unused0", event.unused0)
        int("unused1", event.unused1)
        int("unused2", event.unused2)
    }

    override fun mapProjAnim(message: MapProjAnim) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnim(message)
    }
    private fun Property.buildMapProjAnimV2(event: MapProjAnimV2) {
        scriptVarType("id", ScriptVarType.SPOTANIM, if (event.id == 65535) -1 else event.id)
        int("starttime", event.startTime)
        int("endtime", event.endTime)
        int("angle", event.angle)
        int("progress", event.progress)
        int("startheight", event.startHeight)
        int("endheight", event.endHeight)
        filteredBoolean("followterrain", event.followTerrain)
        filteredBoolean("unusedcoordbit", event.unusedCoordinateBit)
        group("SOURCE") {
            zoneCoord(event.xInZone, event.zInZone)
            projectileOffset(event.startOffset)
        }
        group("TARGET") {
            zoneCoord(event.xInZone + event.deltaX, event.zInZone + event.deltaZ)
            projectileActor(event.target)
            projectileOffset(event.endOffset)
        }
        int("unused0", event.unused0)
        int("unused1", event.unused1)
        int("unused2", event.unused2)
    }

    override fun mapProjAnimV2(message: MapProjAnimV2) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnimV2(message)
    }

    private fun Property.projectileOffset(offset: ProjectileOffset) {
        filteredInt("xoffset", offset.x, 0)
        filteredInt("zoffset", offset.z, 0)
        filteredBoolean("relativeoffset", offset.relative)
        filteredInt("unknownoffsetmode", offset.unknownMode, 0)
    }

    private fun Property.buildMapProjAnimHalfsq(event: MapProjAnimHalfsq) {
        scriptVarType("id", ScriptVarType.SPOTANIM, if (event.id == 65535) -1 else event.id)
        int("starttime", event.startTime)
        int("endtime", event.endTime)
        int("angle", event.angle)
        int("progress", event.progress)
        int("startheight", event.startHeight)
        int("endheight", event.endHeight)
        filteredBoolean("followterrain", event.followTerrain)
        filteredBoolean("finestartheight", event.fineStartHeight)
        filteredInt("unknownflags", event.unknownFlags, 0)
        group("SOURCE") {
            zoneHalfCoord(event.xInZone, event.zInZone)
            projectileActor(event.source)
        }
        group("TARGET") {
            zoneHalfCoord(event.xInZone + event.deltaX, event.zInZone + event.deltaZ)
            projectileActor(event.target)
        }
    }

    private fun Property.projectileActor(actor: Int) {
        // Native projectile lookup uses a type byte and a direct 16-bit index, not OSRS's signed index.
        when (actor ushr 16) {
            1 -> entities.npc(this, actor and 0xFFFF)
            2 -> entities.player(this, actor and 0xFFFF)
            else -> {
                if (actor == 0) {
                    filteredAny<Any>("entity", null, null)
                } else {
                    // Other types resolve to no actor natively; preserve their wire value for inspection.
                    int("unknownactor", actor)
                }
            }
        }
    }

    override fun mapProjAnimHalfsq(message: MapProjAnimHalfsq) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnimHalfsq(message)
    }
    private fun Property.buildMapProjAnimHalfsqV2(event: MapProjAnimHalfsqV2) {
        scriptVarType("id", ScriptVarType.SPOTANIM, if (event.id == 65535) -1 else event.id)
        int("starttime", event.startTime)
        int("endtime", event.endTime)
        int("angle", event.angle)
        int("progress", event.progress)
        int("startheight", event.startHeight)
        int("endheight", event.endHeight)
        filteredBoolean("followterrain", event.followTerrain)
        filteredBoolean("finestartheight", event.fineStartHeight)
        filteredInt("unknownflags", event.unknownFlags, 0)
        group("SOURCE") {
            zoneHalfCoord(event.xInZone, event.zInZone)
            projectileActor(event.source)
            projectileOffset(event.startOffset)
        }
        group("TARGET") {
            zoneHalfCoord(event.xInZone + event.deltaX, event.zInZone + event.deltaZ)
            projectileActor(event.target)
            projectileOffset(event.endOffset)
        }
    }

    override fun mapProjAnimHalfsqV2(message: MapProjAnimHalfsqV2) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnimHalfsqV2(message)
    }
    private fun Property.buildLegacySoundArea(event: LegacySoundArea) {
        scriptVarType("sound", ScriptVarType.SYNTH, event.soundId)
        zoneCoord(event.xInZone, event.zInZone)
        int("loopcount", event.loopCount)
        int("range", event.range)
        filteredInt("rotation", event.rotation, 0)
        filteredInt("heightoffset", event.heightOffset, 0)
    }

    override fun legacySoundArea(message: LegacySoundArea) {
        if (!filters[PropertyFilter.SOUND_AREA]) return omit()
        root.buildLegacySoundArea(message)
    }

    private fun Property.buildTextCoord(event: TextCoord) {
        event.ignored?.let { int("ignored", it) }
        zoneCoord(event.xInZone, event.zInZone)
        int("height", event.height)
        int("duration", event.duration)
        any("rgb", "#%06x".format(event.rgb))
        string("text", event.text)
    }

    override fun textCoord(message: TextCoord) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildTextCoord(message)
    }

    private fun Property.buildLegacyMapProjAnim(event: LegacyMapProjAnim) {
        val world = sessionState.getActiveWorld()
        val c = world.relativizeZoneCoord(event.xInZone, event.zInZone)
        val dest = world.relativizeZoneCoord(event.xInZone + event.targetDeltaX, event.zInZone + event.targetDeltaY)
        scriptVarType("id", ScriptVarType.SPOTANIM, event.id)
        coordGrid("coord", c)
        coordGrid("destcoord", dest)
        int("startheight", event.startHeight)
        int("endheight", event.endHeight)
        int("starttime", event.startTime)
        int("endtime", event.endTime)
        filteredInt("alpha", event.alpha, 0)
        filteredInt("lockonslot", event.lockonSlot, 0)
    }

    override fun legacyMapProjAnim(message: LegacyMapProjAnim) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildLegacyMapProjAnim(message)
    }

    private fun Property.buildLegacyMapProjAnimHalfsq(event: LegacyMapProjAnimHalfsq) {
        scriptVarType("id", ScriptVarType.SPOTANIM, event.id)
        zoneCoord(event.xInZone, event.zInZone)
        int("destxdeltahalf", event.destXdeltaHalf)
        int("destydeltahalf", event.destYdeltaHalf)
        if (event.trailingBytes.isNotEmpty()) {
            any("trailingbytes", hex(event.trailingBytes))
        }
    }

    override fun legacyMapProjAnimHalfsq(message: LegacyMapProjAnimHalfsq) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildLegacyMapProjAnimHalfsq(message)
    }

    private fun Property.buildLegacyMapProjAnimHalfsqV2(event: LegacyMapProjAnimHalfsqV2) {
        scriptVarType("spotanimid", ScriptVarType.SPOTANIM, event.spotAnimId)
        zoneHalfCoord(event.xInZoneHalf, event.zInZoneHalf, "startcoord")
        zoneHalfCoord(event.xInZoneHalf + event.destXdeltaHalf, event.zInZoneHalf + event.destYdeltaHalf, "destcoord")
        any("source", formatEntityRef(event.sourceType, event.sourceIndex))
        any("target", formatEntityRef(event.targetType, event.targetIndex))
        int("startheight", event.startHeight)
        int("endheight", event.endHeight)
        int("starttime", event.startTime)
        int("endtime", event.endTime)
        filteredInt("alpha?", event.alpha, 0)
        int("angle?", event.angle)
        int("flags?", event.flags)
        any("startOffset?", "0x%06x".format(event.startOffset))
        any("endOffset?", "0x%06x".format(event.endOffset))
    }

    private fun formatEntityRef(
        kind: Int,
        index: Int,
    ): String {
        return when (kind) {
            1 -> sessionState.npcLabel(index)
            2 -> sessionState.playerLabel(index)
            10 -> "Follow Source"
            255 -> "NONE"
            else -> "UNK_$kind($index)"
        }
    }

    override fun legacyMapProjAnimHalfsqV2(message: LegacyMapProjAnimHalfsqV2) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildLegacyMapProjAnimHalfsqV2(message)
    }

    override fun legacyMapProjAnimV2(message: LegacyMapProjAnimV2) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        val c = sessionState.getActiveWorld().relativizeZoneCoord(message.xInZone, message.zInZone)
        root.scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
        root.coordGrid("coord", c)
        root.int("targetdeltax", message.targetDeltaX)
        root.int("targetdeltay", message.targetDeltaY)
        if (message.trailingBytes.isNotEmpty()) {
            root.any("trailingbytes", hex(message.trailingBytes))
        }
    }


    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        val includeZoneHeader = filters[PropertyFilter.ZONE_HEADER]
        if (includeZoneHeader) {
            root.buildZoneFollowsCommon(message.level, message.zoneX, message.zoneZ)
        } else {
            omit()
        }
        if (message.packets.isEmpty()) {
            return
        }
        if (includeZoneHeader) {
            createChildZoneProts(root, message.packets)
        } else {
            createFakeZoneProts(message.packets)
        }
    }

    private fun createChildZoneProts(
        root: Property,
        packets: List<IncomingServerGameMessage>,
    ) {
        for (event in packets) {
            when (event) {
                is MapProjAnimHalfsqV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_HALFSQ_V2") { buildMapProjAnimHalfsqV2(event) }
                }
                is MapProjAnimHalfsq -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_HALFSQ") { buildMapProjAnimHalfsq(event) }
                }
                is MapProjAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_V2") { buildMapProjAnimV2(event) }
                }
                is MapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM") { buildMapProjAnim(event) }
                }
                is SoundAreaV2 -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    root.group("SOUND_AREA_V2") { buildSoundAreaV2(event) }
                }
                is SoundAreaV1 -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    root.group("SOUND_AREA_V1") { buildSoundAreaV1(event) }
                }
                is LocAnim -> {
                    if (!filters[PropertyFilter.LOC_ANIM]) continue
                    root.group("LOC_ANIM") { buildLocAnim(event) }
                }
                is LocAddChange -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    root.group("LOC_ADD_CHANGE") { buildLocAddChange(event) }
                }
                is LocCustomise -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    root.group("LOC_CUSTOMISE") { buildLocCustomise(event) }
                }
                is LocPrefetch -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    root.group("LOC_PREFETCH") { buildLocPrefetch(event) }
                }
                is LegacyMidiSongLocation -> {
                    if (!filters[PropertyFilter.MIDI_SONG]) continue
                    root.group("MIDI_SONG_LOCATION") { buildLegacyMidiSongLocation(event) }
                }
                is LocDel -> {
                    if (!filters[PropertyFilter.LOC_DEL]) continue
                    root.group("LOC_DEL") { buildLocDel(event) }
                }
                is ObjAdd -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    root.group(if (event.big) "OBJ_ADD_V2" else "OBJ_ADD") { buildObjAdd(event) }
                }
                is ObjDel -> {
                    if (!filters[PropertyFilter.OBJ_DEL]) continue
                    root.group(if (event.big) "OBJ_DEL_V2" else "OBJ_DEL") { buildObjDel(event) }
                }
                is ObjCount -> {
                    if (!filters[PropertyFilter.OBJ_COUNT]) continue
                    root.group(if (event.big) "OBJ_COUNT_V2" else "OBJ_COUNT") { buildObjCount(event) }
                }
                is ObjReveal -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    root.group(if (event.big) "OBJ_REVEAL_V2" else "OBJ_REVEAL") { buildObjReveal(event) }
                }
                is MapAnim -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("MAP_ANIM") { buildMapAnim(event) }
                }
                is MapAnimV1 -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("MAP_ANIM_V1") { buildMapAnimV1(event) }
                }
                is MapAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("MAP_ANIM_V2") { buildMapAnimV2(event) }
                }
                is LegacySoundArea -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    root.group("SOUND_AREA") { buildLegacySoundArea(event) }
                }
                is TextCoord -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("TEXT_COORD") { buildTextCoord(event) }
                }
                is LegacyMapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM") { buildLegacyMapProjAnim(event) }
                }
                is LegacyMapProjAnimHalfsq -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_HALFSQ") { buildLegacyMapProjAnimHalfsq(event) }
                }
                is LegacyMapProjAnimHalfsqV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_HALFSQ_V2") { buildLegacyMapProjAnimHalfsqV2(event) }
                }
                is LegacyMapProjAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_V2") { buildLegacyMapProjAnimV2(event) }
                }
                else -> Unit
            }
        }
    }

    private fun createFakeZoneProts(packets: List<IncomingServerGameMessage>) {
        for (event in packets) {
            when (event) {
                is MapProjAnimHalfsqV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_HALFSQ_V2").buildMapProjAnimHalfsqV2(event)
                }
                is MapProjAnimHalfsq -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_HALFSQ").buildMapProjAnimHalfsq(event)
                }
                is MapProjAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_V2").buildMapProjAnimV2(event)
                }
                is MapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM").buildMapProjAnim(event)
                }
                is SoundAreaV2 -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    sessionState.createFakeServerRoot("SOUND_AREA_V2").buildSoundAreaV2(event)
                }
                is SoundAreaV1 -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    sessionState.createFakeServerRoot("SOUND_AREA_V1").buildSoundAreaV1(event)
                }
                is LocAnim -> {
                    if (!filters[PropertyFilter.LOC_ANIM]) continue
                    sessionState.createFakeServerRoot("LOC_ANIM").buildLocAnim(event)
                }
                is LocAddChange -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    sessionState.createFakeServerRoot("LOC_ADD_CHANGE").buildLocAddChange(event)
                }
                is LocCustomise -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    sessionState.createFakeServerRoot("LOC_CUSTOMISE").buildLocCustomise(event)
                }
                is LocPrefetch -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    sessionState.createFakeServerRoot("LOC_PREFETCH").buildLocPrefetch(event)
                }
                is LegacyMidiSongLocation -> {
                    if (!filters[PropertyFilter.MIDI_SONG]) continue
                    sessionState.createFakeServerRoot("MIDI_SONG_LOCATION").buildLegacyMidiSongLocation(event)
                }
                is LocDel -> {
                    if (!filters[PropertyFilter.LOC_DEL]) continue
                    sessionState.createFakeServerRoot("LOC_DEL").buildLocDel(event)
                }
                is ObjAdd -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    sessionState.createFakeServerRoot(if (event.big) "OBJ_ADD_V2" else "OBJ_ADD").buildObjAdd(event)
                }
                is ObjDel -> {
                    if (!filters[PropertyFilter.OBJ_DEL]) continue
                    sessionState.createFakeServerRoot(if (event.big) "OBJ_DEL_V2" else "OBJ_DEL").buildObjDel(event)
                }
                is ObjCount -> {
                    if (!filters[PropertyFilter.OBJ_COUNT]) continue
                    sessionState.createFakeServerRoot(if (event.big) "OBJ_COUNT_V2" else "OBJ_COUNT").buildObjCount(event)
                }
                is ObjReveal -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    sessionState.createFakeServerRoot(if (event.big) "OBJ_REVEAL_V2" else "OBJ_REVEAL")
                        .buildObjReveal(event)
                }
                is MapAnim -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("MAP_ANIM").buildMapAnim(event)
                }
                is MapAnimV1 -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("MAP_ANIM_V1").buildMapAnimV1(event)
                }
                is MapAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("MAP_ANIM_V2").buildMapAnimV2(event)
                }
                is LegacySoundArea -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    sessionState.createFakeServerRoot("SOUND_AREA").buildLegacySoundArea(event)
                }
                is TextCoord -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("TEXT_COORD").buildTextCoord(event)
                }
                is LegacyMapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM").buildLegacyMapProjAnim(event)
                }
                is LegacyMapProjAnimHalfsq -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_HALFSQ").buildLegacyMapProjAnimHalfsq(event)
                }
                is LegacyMapProjAnimHalfsqV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_HALFSQ_V2").buildLegacyMapProjAnimHalfsqV2(event)
                }
                is LegacyMapProjAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_V2").buildLegacyMapProjAnimV2(event)
                }
                else -> Unit
            }
        }
    }

    private fun Property.buildLegacyMapProjAnimV2(event: LegacyMapProjAnimV2) {
        zoneCoord(event.xInZone, event.zInZone)
        int("targetdeltax", event.targetDeltaX)
        int("targetdeltay", event.targetDeltaY)
        int("idmedium", event.idMedium)
        scriptVarType("id", ScriptVarType.SPOTANIM, event.id)
        any("trailingbytes", hex(event.trailingBytes))
    }

    override fun varcSmall(message: VarcSmall) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.varc("varc", message.id)
        root.int("value", message.value)
    }

    override fun varcLarge(message: VarcLarge) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.varc("varc", message.id)
        root.int("value", message.value)
    }

    override fun varcBitSmall(message: VarcBitSmall) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.int("varcbit", message.id)
        root.int("value", message.value)
    }

    override fun varcBitLarge(message: VarcBitLarge) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.int("varcbit", message.id)
        root.int("value", message.value)
    }

    override fun varcStrSmall(message: VarcStrSmall) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.varc("varc", message.id)
        root.string("value", message.value)
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
        if (!filters[PropertyFilter.IF_SETNPCHEAD]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.NPC, message.npcId)
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.component("com", message.componentHash)
    }

    override fun ifSetText(message: IfSetText) {
        if (!filters[PropertyFilter.IF_SETTEXT]) return omit()
        root.component("com", message.componentHash)
        root.string("text", message.text)
    }

    override fun ifSetObject(message: IfSetObject) {
        if (!filters[PropertyFilter.IF_SETOBJECT]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.OBJ, message.objId)
        root.int("zoomorcount", message.count)
    }

    override fun ifOpenSubActiveObj(message: IfOpenSubActiveObj) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.INTERFACE, message.childId)
        root.scriptVarType("obj", ScriptVarType.OBJ, message.objId)
        root.ifType(message.layer)
        root.int("coordinate", message.coord)
        root.xteas(message.extra1, message.extra2, message.extra3, message.extra4)
    }

    override fun ifOpenSubActiveLoc(message: IfOpenSubActiveLoc) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.INTERFACE, message.childId)
        root.scriptVarType("loc", ScriptVarType.LOC, message.locId)
        root.scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        root.int("rotation", message.rotation)
        root.ifType(message.layer)
        root.int("coordinate", message.coord)
        root.xteas(message.extra1, message.extra2, message.extra3, message.extra4)
    }

    override fun ifSetModel(message: IfSetModel) {
        if (!filters[PropertyFilter.IF_SETMODEL]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.MODEL, message.modelId)
    }

    override fun ifSetPosition(message: IfSetPosition) {
        if (!filters[PropertyFilter.IF_SETPOSITION]) return omit()
        root.component("com", message.componentHash)
        root.int("x", message.x)
        root.int("y", message.y)
    }

    override fun ifSetAnim(message: IfSetAnim) {
        if (!filters[PropertyFilter.IF_SETANIM]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.SEQ, message.animId)
    }

    override fun ifSetColour(message: IfSetColour) {
        if (!filters[PropertyFilter.IF_SETCOLOUR]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("colour", ScriptVarType.COLOUR, message.packedColor)
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
        if (!filters[PropertyFilter.IF_SETSCROLLPOS]) return omit()
        root.component("com", message.componentHash)
        root.int("scrollpos", message.scrollPos)
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.component("com", message.componentHash)
    }

    override fun ifSetTargetParam(message: IfSetTargetParam) {
        if (!filters[PropertyFilter.IF_SETEVENTS]) return omit()
        root.component("com", message.componentHash)
        root.int("targetparam", message.targetParam)
        root.int("fromslot", if (message.fromSlot == 65535) -1 else message.fromSlot)
        root.int("toslot", if (message.toSlot == 65535) -1 else message.toSlot)
    }

    override fun ifSetEvents(message: IfSetEvents) {
        if (!filters[PropertyFilter.IF_SETEVENTS]) return omit()
        root.component("com", message.componentHash)
        root.int("start", if (message.fromSlot == 65535) -1 else message.fromSlot)
        root.int("end", if (message.toSlot == 65535) -1 else message.toSlot)
        root.any("events", Rs3InterfaceEvents.list(message.settings).toString())
    }

    override fun camLookAt(message: CamLookAt) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.int("localx", message.localX)
        root.int("localz", message.localZ)
        root.int("height", message.height)
        root.int("speed", message.speed)
        root.int("accel", message.accel)
    }

    override fun camShake(message: CamShake) {
        if (!filters[PropertyFilter.CAM_SHAKE]) return omit()
        root.int("axis", message.axis)
        root.int("random", message.randomAmplitude)
        root.int("amplitude", message.sineAmplitude)
        root.int("rate", message.frequency)
        root.int("duration", message.duration)
    }

    override fun legacyCamShake(message: LegacyCamShake) {
        if (!filters[PropertyFilter.CAM_SHAKE]) return omit()
        root.int("shakemode", message.shakeMode)
        root.int("param0", message.param0)
        root.int("param1", message.param1)
        root.int("param2", message.param2)
        root.int("param3", message.param3)
    }

    override fun camForceAngle(message: CamForceAngle) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.int("angle0", message.angle0)
        root.int("angle1", message.angle1)
    }

    override fun legacyCamForceAngle(message: LegacyCamForceAngle) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.int("yaw", message.yaw)
        root.int("pitch", message.pitch)
    }

    override fun camMoveTo(message: CamMoveTo) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        root.int("localx", message.localX)
        root.int("localz", message.localZ)
        root.int("height", message.height)
        root.int("speed", message.speed)
        root.int("accel", message.accel)
    }

    override fun noTimeout(message: NoTimeout) {
        if (!filters[PropertyFilter.NO_TIMEOUT]) omit()
    }

    override fun tickEnd(message: TickEnd) {
        if (!filters[PropertyFilter.SERVER_TICK_END]) omit()
    }

    override fun ifSetPlayerHeadIgnoreWorn(message: IfSetPlayerHeadIgnoreWorn) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.component("com", message.componentHash)
        root.int("kitlow", message.kitLow)
        root.int("kitextra", message.kitExtra)
        root.int("kithigh", message.kitHigh)
    }

    override fun ifSetPlayerHeadOther(message: IfSetPlayerHeadOther) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.component("com", message.componentHash)
        root.int("appearancehash", message.appearanceHash)
        root.int("playerindex", message.playerIndex)
    }

    override fun ifSetPlayerModelOther(message: IfSetPlayerModelOther) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.component("com", message.componentHash)
        root.int("appearancehash", message.appearanceHash)
        root.int("playerindex", message.playerIndex)
    }

    override fun ifSetAngle(message: IfSetAngle) {
        if (!filters[PropertyFilter.IF_SETANGLE]) return omit()
        root.component("com", message.componentHash)
        root.int("angle0", message.angle0)
        root.int("angle1", message.angle1)
        root.int("zoom", message.zoom)
    }

    override fun ifSetTextAntiMacro(message: IfSetTextAntiMacro) {
        if (!filters[PropertyFilter.IF_SETTEXTANTIMACRO]) return omit()
        root.component("com", message.componentHash)
        root.boolean("enabled", message.enabled)
    }

    override fun ifSetClickMask(message: IfSetClickMask) {
        if (!filters[PropertyFilter.IF_SETCLICKMASK]) return omit()
        root.component("com", message.componentHash)
        root.boolean("enabled", message.enabled)
    }

    override fun ifSetTextFont(message: IfSetTextFont) {
        if (!filters[PropertyFilter.IF_SETTEXTFONT]) return omit()
        root.component("com", message.componentHash)
        root.int("font", message.fontId)
    }

    override fun ifSetGraphic(message: IfSetGraphic) {
        if (!filters[PropertyFilter.IF_SETGRAPHIC]) return omit()
        root.component("com", message.componentHash)
        root.int("graphic", message.graphicId)
    }

    override fun ifSetRecol(message: IfSetRecol) {
        if (!filters[PropertyFilter.IF_SETRECOL]) return omit()
        root.component("com", message.componentHash)
        root.int("index", message.index)
        root.int("source", message.source)
        root.int("destination", message.destination)
    }

    override fun ifSetRetex(message: IfSetRetex) {
        if (!filters[PropertyFilter.IF_SETRETEX]) return omit()
        root.component("com", message.componentHash)
        root.int("index", message.index)
        root.int("source", message.source)
        root.int("destination", message.destination)
    }

    override fun ifMoveSub(message: IfMoveSub) {
        if (!filters[PropertyFilter.IF_MOVESUB]) return omit()
        root.component("sourcecom", message.source)
        root.component("destcom", message.destination)
        sessionState.getOpenInterface(message.source)?.let {
            root.scriptVarType("id", ScriptVarType.INTERFACE, it)
        }
    }

    override fun ifSetHttpImage(message: IfSetHttpImage) {
        if (!filters[PropertyFilter.IF_SET_HTTP_IMAGE]) return omit()
        root.component("com", message.componentHash)
        root.int("imageid", message.imageId)
    }

    override fun ifSetObjectLongV2(message: IfSetObjectLongV2) {
        if (!filters[PropertyFilter.IF_SETOBJECT]) return omit()
        root.component("com", message.componentHash)
        root.scriptVarType("id", ScriptVarType.OBJ, message.objId)
        root.long("zoomorcount", message.quantity)
    }

    override fun setNpcAttackPriority(message: SetNpcAttackPriority) {
        if (!filters[PropertyFilter.ATTACK_PRIORITY]) return omit()
        root.int("priority", message.priority)
    }

    override fun setPlayerAttackPriority(message: SetPlayerAttackPriority) {
        if (!filters[PropertyFilter.ATTACK_PRIORITY]) return omit()
        root.int("priority", message.priority)
    }

    override fun setTarget(message: SetTarget) {
        if (!filters[PropertyFilter.SET_TARGET]) return omit()
        entities.entity(root, message.target)
    }

    override fun loyaltyUpdate(message: LoyaltyUpdate) {
        if (!filters[PropertyFilter.LOYALTY_UPDATE]) return omit()
        root.int("loyaltypoints", message.loyaltyPoints)
    }

    override fun syncClock(message: SyncClock) {
        if (!filters[PropertyFilter.SYNC_CLOCK]) return omit()
        root.long("servertime", message.serverTime)
    }

    override fun camRemoveRoof(message: CamRemoveRoof) {
        if (!filters[PropertyFilter.CAM_REMOVEROOF]) return omit()
        root.int("coordinate", message.coordinate)
    }

    override fun pointLightExtendAbove(message: PointLightExtendAbove) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("id", message.id)
        root.int("mode", message.mode)
    }

    override fun pointLightExtendBelow(message: PointLightExtendBelow) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("id", message.id)
        root.int("mode", message.mode)
    }

    override fun pointLightAttenuationFalloff(message: PointLightAttenuationFalloff) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("falloff", message.falloff)
        root.int("id", message.id)
    }

    override fun pointLightIntensityScale(message: PointLightIntensityScale) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("intensity", message.intensity)
        root.int("duration", message.duration)
        root.int("id", message.id)
    }

    override fun pointLightColour(message: PointLightColour) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("colour", message.colour)
        root.int("duration", message.duration)
        root.int("id", message.id)
    }

    override fun pointLightEnabled(message: PointLightEnabled) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("encodedcontrol", message.encodedControl)
        root.int("id", message.id)
        root.int("mode", message.mode)
        root.boolean("preserveintensity", message.preserveIntensity)
    }

    override fun pointLightShadow(message: PointLightShadow) {
        if (!filters[PropertyFilter.POINTLIGHT]) return omit()
        root.int("id", message.id)
        root.int("encodedcontrol", message.encodedControl)
        root.int("mode", message.mode)
    }

    override fun cameraUpdate(message: CameraUpdate) {
        if (!filters[PropertyFilter.CAMERA_UPDATE]) return omit()
        root.appendCameraUpdate(message)
    }

    override fun legacyCameraUpdate(message: LegacyCameraUpdate) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        root.any("headerflags", "0x${message.headerFlags.toString(16)}")
        root.any("bitmask", "0x${message.bitmask.toString(16)}")
    }

    override fun updateInvFull(message: UpdateInvFull) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.scriptVarType("id", ScriptVarType.INV, message.inventoryId)
        root.filteredBoolean("secondary", message.flags and 0x1 != 0)
        root.filteredBoolean("hasvarobj", message.flags and 0x2 != 0)
        root.filteredInt("unknownflags", message.flags and 0xFC, 0)
        root.group("OBJS") {
            for (obj in message.objs) {
                group {

                    scriptVarType("id", ScriptVarType.OBJ, obj.id)
                    formattedInt("count", obj.count)
                    if (obj.vars.isNotEmpty()) {
                        group("VAROBJ") {
                            for (variable in obj.vars) {
                                group {
                                    varobj("id", variable.varId)
                                    int("value", variable.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.scriptVarType("id", ScriptVarType.INV, message.inventoryId)
        root.filteredBoolean("secondary", message.flags and 0x1 != 0)
        root.filteredInt("unknownflags", message.flags and 0xFE, 0)
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.scriptVarType("id", ScriptVarType.INV, message.inventoryId)
        root.filteredBoolean("secondary", message.flags and 0x1 != 0)
        root.filteredBoolean("hasvarobj", message.flags and 0x2 != 0)
        root.filteredInt("unknownflags", message.flags and 0xFC, 0)
        root.group("OBJS") {
            for (obj in message.objs) {
                group {
                    int("slot", obj.slot)
                    scriptVarType("id", ScriptVarType.OBJ, obj.id)
                    formattedInt("count", obj.count)
                    if (obj.vars.isNotEmpty()) {
                        group("VAROBJ") {
                            for (variable in obj.vars) {
                                group {
                                    varobj("id", variable.varId)
                                    int("value", variable.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
        if (!filters[PropertyFilter.UPDATE_RUNWEIGHT]) return omit()
        root.formattedInt("weight", message.weight, KG_NUMBER_FORMAT)
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
        if (!filters[PropertyFilter.UPDATE_RUNENERGY]) return omit()
        root.int("energy", message.energy)
    }

    override fun updateStat(message: UpdateStat) {
        if (!filters[PropertyFilter.UPDATE_STAT]) return omit()
        val gained = sessionState.getExperience(message.skillId)?.let { message.xp - it }
        val skill = Rs3Stat.entries.getOrNull(message.skillId)
        if (skill != null) {
            root.namedEnum("skill", skill)
        } else {
            root.int("skill", message.skillId)
        }
        root.int("level", message.level)
        root.int("xp", message.xp)
        if (gained != null) {
            root.long("xpgained", gained)
        }
    }

    override fun minimapToggle(message: MinimapToggle) {
        if (!filters[PropertyFilter.MINIMAP_TOGGLE]) return omit()
        val state = message.minimapState
        val modeName =
            when (state % 3) {
                0 -> "NORMAL"
                1 -> "CLICK_DISABLED"
                2 -> "BLACKOUT"
                else -> "UNKNOWN"
            }
        root.any("state", "$modeName($state)")
        root.boolean("interactive", state < 3)
    }

    override fun hintTrail(message: HintTrail) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        root.int("slot", message.slot)
        root.scriptVarType("model", ScriptVarType.MODEL, message.modelId)
        message.trail?.let { trail ->
            val level = sessionState.level()
            root.group("TRAIL") {
                coordGrid(level, trail.baseX, trail.baseZ, "base")
                filteredInt("count", trail.count, trail.points.size)
                for (point in trail.points) {
                    group("WAYPOINT") {
                        coordGrid(level, trail.baseX + point.deltaX, trail.baseZ + point.deltaZ)
                    }
                }
            }
        }
    }

    override fun legacyHintTrail(message: LegacyHintTrail) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        root.int("slot", message.slot)
        root.scriptVarType("model", ScriptVarType.MODEL, message.modelId)
    }

    override fun hintArrow(message: HintArrow) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        root.int("slot", message.slot)
        when (val payload = message.payload) {
            is HintArrow.Clear -> {
                root.any("type", "reset")
                root.filteredAny("reserved", payload.reserved, List(payload.reserved.size) { 0 })
            }
            is HintArrow.Actor -> {
                when (message.kind) {
                    1 -> entities.npc(root, payload.index)
                    10 -> entities.player(root, payload.index)
                    else -> {
                        root.int("kind", message.kind)
                        root.int("index", payload.index)
                    }
                }
                root.int("sprite", payload.sprite)
                root.int("parameter", payload.parameter)
                root.int("parameter32", payload.parameter32)
                root.filteredAny("reserved", payload.reserved, List(payload.reserved.size) { 0 })
            }
            is HintArrow.Location -> {
                root.coordGrid(payload.level, payload.x, payload.z)
                root.int("height", payload.height)
                root.string(
                    "position",
                    when (message.kind) {
                        2 -> "Center"
                        3 -> "West"
                        4 -> "East"
                        5 -> "South"
                        6 -> "North"
                        else -> "Unknown"
                    },
                )
                root.int("sprite", payload.sprite)
                root.int("range", payload.range)
                root.int("parameter32", payload.parameter32)
            }
            is HintArrow.Other -> {
                root.int("kind", message.kind)
                root.int("sprite", payload.sprite)
                root.int("parameter32", payload.parameter32)
                root.filteredAny("reserved", payload.reserved, List(payload.reserved.size) { 0 })
            }
        }
    }

    override fun legacyHintArrow(message: LegacyHintArrow) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        root.int("slot", message.slot)
        if (message.isReset) {
            root.string("type", "RESET")
            return
        }
        root.int("type", message.type)
        root.int("targetindex", message.targetIndex ?: -1)
        root.int("x", message.x ?: -1)
        root.int("y", message.y ?: -1)
        root.int("z", message.z ?: -1)
        root.int("distance", message.distance ?: -1)
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
        if (!filters[PropertyFilter.CHAT_FILTER_SETTINGS]) return omit()
        root.int("private", message.privateChatFilter)
    }

    override fun setPlayerOp(message: SetPlayerOp) {
        if (!filters[PropertyFilter.SET_PLAYER_OP]) return omit()
        root.int("id", message.slot)
        if (message.text.equals("null", ignoreCase = true)) {
            root.any<Any>("op", null)
        } else {
            root.string("op", message.text)
        }
        root.filteredBoolean("priority", message.priority)
        root.any("cursor", message.cursor.takeUnless { it == -1 })
    }

    override fun legacySetPlayerOp(message: LegacySetPlayerOp) {
        if (!filters[PropertyFilter.SET_PLAYER_OP]) return omit()
        root.int("slot", message.slot)
        root.string("text", message.text)
        root.boolean("priority", message.priority)
        root.int("worldid", message.worldId)
    }

    override fun ifSetPlayerModelSnapshot(message: IfSetPlayerModelSnapshot) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.component("com", message.componentHash)
        root.int("snapshotslot", message.snapshotSlot)
    }

    override fun ifSetPlayerHeadSnapshot(message: IfSetPlayerHeadSnapshot) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.component("com", message.componentHash)
        root.int("snapshotslot", message.snapshotSlot)
    }

    override fun vorbisSound(message: VorbisSound) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.scriptVarType("sound", ScriptVarType.SYNTH, message.soundId)
        root.int("volume", message.volume)
        root.int("loops", message.loops)
        root.int("delay", message.delay)
        root.int("pitch", message.pitch)
    }

    override fun runClientScript(message: RunClientScript) {
        if (!filters[PropertyFilter.RUNCLIENTSCRIPT]) return omit()
        root.script("id", message.id)
        root.any("types", String(message.types))
        root.group("PARAMS") {
            for ((index, value) in message.values.withIndex()) {
                group {
                    val type =
                        when (value) {
                            is String -> ScriptVarType.STRING
                            is Long -> ScriptVarType.LONG
                            is Int -> {
                                ScriptVarType.entries.firstOrNull {
                                    it.char == message.types[index] && it.baseVarType == BaseVarType.INTEGER
                                } ?: ScriptVarType.INT
                            }
                            else -> error("Unsupported clientscript parameter type: ${value.javaClass.name}")
                        }
                    enum("type", type)
                    scriptVarType("value", type, value)
                }
            }
        }
    }

    override fun projAnimSpecificV2(message: ProjAnimSpecificV2) {
        if (!filters[PropertyFilter.PROJANIM_SPECIFIC]) return omit()
        root.scriptVarType("id", ScriptVarType.SPOTANIM, if (message.id == 65535) -1 else message.id)
        root.int("starttime", message.startTime)
        root.int("endtime", message.endTime)
        root.int("angle", message.angle)
        root.int("progress", message.progress)
        root.int("startheight", message.startHeight)
        root.int("endheight", message.endHeight)
        root.filteredBoolean("followterrain", message.followTerrain)
        root.filteredBoolean("finestartheight", message.fineStartHeight)
        root.filteredInt("unknownflags", message.unknownFlags, 0)
        root.group("SOURCE") {
            specificHalfCoord(message.level, message.startX, message.startZ)
            projectileActor(message.source)
            projectileOffset(message.startOffset)
        }
        root.group("TARGET") {
            specificHalfCoord(message.level, message.startX + message.deltaX, message.startZ + message.deltaZ)
            projectileActor(message.target)
            projectileOffset(message.endOffset)
        }
    }

    private fun Property.specificHalfCoord(level: Int, x: Int, z: Int) {
        // Wire positions/deltas are absolute half-tiles, not zone-relative or whole tiles.
        if (level in 0..3 && x in 0..32767 && z in 0..32767) {
            coordinates.appendFine(this, level, x shr 1, z shr 1, (x and 1) * 64, (z and 1) * 64, "coord")
        } else {
            group("coord") {
                int("level", level)
                int("2x", x)
                int("2z", z)
            }
        }
    }

    override fun legacyProjAnimSpecificV2(message: LegacyProjAnimSpecificV2) {
        if (!filters[PropertyFilter.PROJANIM_SPECIFIC]) return omit()
        root.scriptVarType("spotanim", ScriptVarType.SPOTANIM, message.spotAnimId)
        root.int("field1", message.field1)
        root.int("field2", message.field2)
        root.int("field4", message.field4)
        root.int("field5", message.field5)
        root.int("field6", message.field6)
        root.int("field7", message.field7)
        root.int("field8", message.field8)
        root.int("field9", message.field9)
        root.int("field10", message.field10)
        root.int("field11", message.field11)
        root.int("field12", message.field12)
        root.int("field13", message.field13)
        root.int("field14", message.field14)
        root.int("field15", message.field15)
        root.int("field16", message.field16)
        root.int("field17", message.field17)
    }

    override fun locPrefetch(message: LocPrefetch) {
        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) return omit()
        root.buildLocPrefetch(message)
    }

    override fun cutscene2dPlay(message: Cutscene2dPlay) {
        if (!filters[PropertyFilter.CUTSCENE2D_PLAY]) return omit()
        root.int("id", message.id)
    }

    override fun jcoinsUpdate(message: JcoinsUpdate) {
        if (!filters[PropertyFilter.JCOINS_UPDATE]) return omit()
        root.int("jcoins", message.jcoins)
    }

    override fun logout(message: Logout) {
        if (!filters[PropertyFilter.LOGOUT]) return omit()
        root.int("reason", message.reason)
    }

    override fun friendlistLoaded(message: FriendlistLoaded) {
        if (!filters[PropertyFilter.FRIENDLIST_LOADED]) return omit()
    }

    override fun varclanEnable(message: VarclanEnable) {
        if (!filters[PropertyFilter.VARCLAN]) return omit()
    }

    override fun varclanDisable(message: VarclanDisable) {
        if (!filters[PropertyFilter.VARCLAN]) return omit()
    }

    override fun resetAnims(message: ResetAnims) {
        if (!filters[PropertyFilter.RESET_ANIMS]) return omit()
    }

    override fun locSelectClear(message: LocSelectClear) {
        if (!filters[PropertyFilter.LOC_SELECTION]) return omit()
    }

    override fun storeServerpermVarcsAck(message: StoreServerpermVarcsAck) {
        if (!filters[PropertyFilter.STORE_CONTROL]) return omit()
    }

    override fun js5Reload(message: Js5Reload) {
        if (!filters[PropertyFilter.JS5_RELOAD]) return omit()
    }

    override fun storeReset(message: StoreReset) {
        if (!filters[PropertyFilter.STORE_CONTROL]) return omit()
    }

    override fun createCheckNameReply(message: CreateCheckNameReply) {
        if (!filters[PropertyFilter.ACCOUNT_CREATION]) return omit()
        root.int("response", message.response)
    }

    override fun createCheckEmailReply(message: CreateCheckEmailReply) {
        if (!filters[PropertyFilter.ACCOUNT_CREATION]) return omit()
        root.int("response", message.response)
    }

    override fun createAccountReply(message: CreateAccountReply) {
        if (!filters[PropertyFilter.ACCOUNT_CREATION]) return omit()
        root.int("response", message.response)
    }

    override fun createSuggestNameError(message: CreateSuggestNameError) {
        if (!filters[PropertyFilter.ACCOUNT_CREATION]) return omit()
        root.int("response", message.response)
    }

    override fun updateDob(message: UpdateDob) {
        if (!filters[PropertyFilter.UPDATE_DOB]) return omit()
        root.int("dateofbirth", message.dateOfBirth)
        root.boolean("verified", message.verified)
    }

    override fun executeClientCheat(message: ExecuteClientCheat) {
        if (!filters[PropertyFilter.EXECUTE_CLIENT_CHEAT]) return omit()
        root.int("command", message.command)
    }

    override fun clearPlayerSnapshot(message: ClearPlayerSnapshot) {
        if (!filters[PropertyFilter.CLEAR_PLAYER_SNAPSHOT]) return omit()
        root.int("index", message.index)
    }

    override fun telemetryGridAddColumn(message: TelemetryGridAddColumn) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
        root.int("id", message.id)
        root.int("index", message.index)
    }

    override fun telemetryGridRemoveRow(message: TelemetryGridRemoveRow) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
        root.int("row", message.row)
    }

    override fun telemetryGridSetRowPinned(message: TelemetryGridSetRowPinned) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
        root.int("row", message.row)
        root.boolean("enabled", message.enabled)
    }

    override fun telemetryGridMoveColumn(message: TelemetryGridMoveColumn) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("source", message.source)
        root.int("group", message.group)
        root.int("destination", message.destination)
    }

    override fun telemetryGridAddGroup(message: TelemetryGridAddGroup) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("id", message.id)
        root.int("index", message.index)
    }

    override fun telemetryGridMoveRow(message: TelemetryGridMoveRow) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
        root.int("source", message.source)
        root.int("destination", message.destination)
    }

    override fun telemetryGridAddRow(message: TelemetryGridAddRow) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
        root.int("id", message.id)
        root.int("index", message.index)
    }

    override fun telemetryClearGridValue(message: TelemetryClearGridValue) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
        root.int("column", message.column)
        root.int("row", message.row)
    }

    override fun telemetryGridRemoveGroup(message: TelemetryGridRemoveGroup) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("group", message.group)
    }

    override fun telemetryGridRemoveColumn(message: TelemetryGridRemoveColumn) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.int("column", message.column)
        root.int("group", message.group)
    }

    override fun sendPing(message: SendPing) {
        if (!filters[PropertyFilter.SEND_PING]) return omit()
        root.int("value1", message.echo0)
        root.int("value2", message.echo1)
    }

    override fun updateUid192(message: UpdateUid192) {
        if (!filters[PropertyFilter.UPDATE_UID192]) return omit()
        root.children += AnyProperty("uid", message.uid, List::class.java)
        root.int("crc", message.crc)
        root.boolean("valid", message.valid)
    }

    override fun setMapFlag(message: SetMapFlag) {
        if (!filters[PropertyFilter.SET_MAP_FLAG]) return omit()
        root.int("id", message.id)
        root.int("sourcex", message.sourceX)
        root.int("sourcez", message.sourceZ)
        root.int("targetx", message.targetX)
        root.int("targetz", message.targetZ)
        root.int("type", message.type)
        root.int("flags", message.flags)
    }

    override fun locSelectConfigure(message: LocSelectConfigure) {
        if (!filters[PropertyFilter.LOC_SELECTION]) return omit()
        root.int("id", message.id)
        root.boolean("enabled", message.enabled)
        root.int("from", message.from)
        root.int("count", message.count)
        root.int("to", message.to)
    }

    override fun locAnimSpecific(message: LocAnimSpecific) {
        if (!filters[PropertyFilter.LOC_ANIM_SPECIFIC]) return omit()
        if (message.coordinate == -1) {
            root.any<Any>("coord", null)
        } else {
            root.coordGrid("coord", CoordGrid(message.coordinate and 0x3FFFFFFF))
        }
        root.scriptVarType("shape", ScriptVarType.LOC_SHAPE, message.shape)
        root.int("rotation", message.rotation)
        root.scriptVarType("anim", ScriptVarType.SEQ, message.animation)
        root.filteredInt("delay", message.delay, 0)
    }

    override fun npcAnimSpecific(message: NpcAnimSpecific) {
        if (!filters[PropertyFilter.NPC_ANIM_SPECIFIC]) return omit()
        entities.npc(root, message.npc)
        root.appendSequences(
            listOf(message.animation0, message.animation1, message.animation2, message.animation3),
            message.delay,
        )
    }

    override fun playerAnimSpecific(message: PlayerAnimSpecific) {
        if (!filters[PropertyFilter.ANIM_SPECIFIC]) return omit()
        root.appendSequences(
            listOf(message.animation0, message.animation1, message.animation2, message.animation3),
            message.delay,
        )
    }

    override fun npcHeadiconSpecific(message: NpcHeadiconSpecific) {
        if (!filters[PropertyFilter.NPC_HEADICON_SPECIFIC]) return omit()
        entities.npc(root, message.npcIndex)
        root.int("headiconslot", message.slot)
        root.scriptVarType("id", ScriptVarType.GRAPHIC, message.id)
        root.int("spriteindex", message.spriteIndex)
    }

    override fun spotanimSpecific(message: SpotanimSpecific) {
        if (!filters[PropertyFilter.SPOTANIM_SPECIFIC]) return omit()
        buildSpotanimSpecific(message)
    }

    private fun buildSpotanimSpecific(message: SpotanimSpecific) {
        if (message.isMapTarget) {
            root.scriptVarType("id", ScriptVarType.SPOTANIM, message.id)
            root.filteredInt("delay", message.delay, 0)
            root.filteredInt("height", message.height, 0)
            if (message.targetCoord == CoordGrid.INVALID) {
                root.any<Any>("coord", null)
            } else {
                root.coordGrid("coord", message.targetCoord)
            }
            // Map effects use the tile, not the actor slot, including on removal.
            root.filteredInt("unusedslot", message.slot, 0)
        } else {
            if (message.isNpcTarget) {
                entities.npc(root, message.targetIndex)
            } else {
                entities.player(root, message.targetIndex)
            }
            root.int("slot", message.slot)
            root.scriptVarType("spotanim", ScriptVarType.SPOTANIM, message.id)
            root.filteredInt("height", message.height, 0)
            root.filteredInt("delay", message.delay, 0)
            root.filteredInt("unusedtargetbits", message.unusedTargetBits, 0)
        }
        root.filteredInt("rotation", message.rotation, 0)
        root.filteredBoolean("loop", message.loop)
        root.filteredBoolean("independentrotation", message.independentRotation)
        root.filteredInt("unknownflags", message.unknownFlags, 0)
    }

    override fun spotanimSpecificV2(message: SpotanimSpecificV2) {
        if (!filters[PropertyFilter.SPOTANIM_SPECIFIC]) return omit()
        buildSpotanimSpecific(message.effect)
        root.projectileOffset(message.offset)
    }

    override fun setMoveAction(message: SetMoveAction) {
        if (!filters[PropertyFilter.SET_MOVE_ACTION]) return omit()
        message.action?.let { root.string("action", it) }
        root.any("cursor", message.cursor.takeUnless { it == -1 })
    }

    override fun createSuggestNameReply(message: CreateSuggestNameReply) {
        if (!filters[PropertyFilter.ACCOUNT_CREATION]) return omit()
        root.string("name", message.name)
    }

    override fun npcSaySpecific(message: NpcSaySpecific) {
        if (!filters[PropertyFilter.NPC_SAY]) return omit()
        entities.npc(root, message.npcIndex)
        root.string("text", message.text)
        root.filteredInt("colour", message.colour, 0)
        root.filteredInt("effect", message.effect, 0)
    }

    override fun doCheat(message: DoCheat) {
        if (!filters[PropertyFilter.EXECUTE_CLIENT_CHEAT]) return omit()
        root.string("command", message.command)
    }

    override fun logoutTransfer(message: LogoutTransfer) {
        if (!filters[PropertyFilter.LOGOUT]) return omit()
        root.int("world", message.world)
        root.string("host", message.host)
        root.int("port", message.port)
        root.int("alternateport", message.alternatePort)
        root.int("transferflag", message.transferFlag)
    }

    override fun changeLobby(message: ChangeLobby) {
        if (!filters[PropertyFilter.CHANGE_LOBBY]) return omit()
        root.string("host", message.host)
        root.int("world", message.world)
        root.int("port", message.port)
        root.int("alternateport", message.alternatePort)
    }

    override fun setLocOpOverride(message: SetLocOpOverride) {
        if (!filters[PropertyFilter.LOC_SELECTION]) return omit()
        root.int("overridestart", message.overrideStart)
        root.int("operation", message.operation)
        root.string("label", message.label)
        root.any("cursor", message.cursor.takeUnless { it == -1 })
        root.int("overrideend", message.overrideEnd)
    }

    override fun projAnimSpecific(message: ProjAnimSpecific) {
        if (!filters[PropertyFilter.PROJANIM_SPECIFIC]) return omit()
        root.scriptVarType("id", ScriptVarType.SPOTANIM, if (message.id == 65535) -1 else message.id)
        root.int("starttime", message.startTime)
        root.int("endtime", message.endTime)
        root.int("angle", message.angle)
        root.int("progress", message.progress)
        root.int("startheight", message.startHeight)
        root.int("endheight", message.endHeight)
        root.filteredBoolean("followterrain", message.followTerrain)
        root.filteredBoolean("finestartheight", message.fineStartHeight)
        root.filteredInt("unknownflags", message.unknownFlags, 0)
        root.group("SOURCE") {
            specificHalfCoord(message.level, message.startX, message.startZ)
            projectileActor(message.source)
        }
        root.group("TARGET") {
            specificHalfCoord(message.level, message.startX + message.deltaX, message.startZ + message.deltaZ)
            projectileActor(message.target)
        }
    }

    override fun debugServerTriggers(message: DebugServerTriggers) {
        if (!filters[PropertyFilter.DEBUG_SERVER_TRIGGERS]) return omit()
        root.int("interfaceid", message.interfaceId)
        root.int("start", message.start)
        root.int("endexclusive", message.endExclusive)
        root.children += AnyProperty("records", message.records, List::class.java)
    }

    override fun unnamed1(message: Unnamed1) {
        if (!filters[PropertyFilter.UNNAMED_SERVER_RECORDS]) return omit()
        root.int("mode", message.mode)
        message.modeWord?.let {
            root.int("modeword", it)
        }
        message.modeBytes?.let {
            root.children += AnyProperty("modebytes", it, Unnamed1.ModeBytes::class.java)
        }
        root.int("textflag", message.textFlag)
        message.discardedText?.let {
            root.string("discardedtext", it)
        }
    }

    override fun unnamed2(message: Unnamed2) {
        if (!filters[PropertyFilter.UNNAMED_SERVER_RECORDS]) return omit()
        root.int("count", message.count)
        root.children += AnyProperty("records", message.records, List::class.java)
        root.int("discardedfooter", message.discardedFooter)
    }

    override fun updateFriendlist(message: UpdateFriendlist) {
        if (!filters[PropertyFilter.UPDATE_FRIENDLIST]) return omit()
        for (friend in message.friends) {
            root.group(if (friend.world == 0) "OFFLINE_FRIEND" else "ONLINE_FRIEND") {
                string("name", friend.name)
                string("previousname", friend.previousName)
                filteredInt("world", friend.world, 0)
                int("rank", friend.rank)
                int("properties", friend.flags)
                string("notes", friend.note)
                friend.worldName?.let { string("worldname", it) }
                friend.platform?.let { int("platform", it) }
                friend.worldMetadata?.let { int("worldflags", it) }
                int("rename", friend.rename)
            }
        }
    }

    override fun updateIgnorelist(message: UpdateIgnorelist) {
        if (!filters[PropertyFilter.UPDATE_IGNORELIST]) return omit()
        for (ignore in message.ignores) {
            root.group("ADDED_IGNORE") {
                string("name", ignore.name)
                string("previousname", ignore.previousName)
                string("notes", ignore.note)
                int("flags", ignore.flags)
            }
        }
    }

    override fun updateFriendchatChannelFull(message: UpdateFriendchatChannelFull) {
        if (!filters[PropertyFilter.UPDATE_FRIENDCHAT_CHANNEL_FULL]) return omit()
        val channel = message.channel ?: return
        root.string("owner", channel.owner)
        root.string("channelname", channel.name)
        root.int("kickrank", channel.minimumKickRank)
        root.int("aliasflag", channel.aliasFlag)
        channel.alias?.let { root.string("alias", it) }
        root.int("membercount", channel.memberCount)
        channel.members?.let { members ->
            root.group("MEMBERS") {
                for (member in members) {
                    group("") {
                        string("name", member.name)
                        int("world", member.world)
                        string("worldname", member.worldName)
                        int("rank", member.rank)
                        int("aliasflag", member.aliasFlag)
                        member.alias?.let { string("alias", it) }
                    }
                }
            }
        }
    }

    override fun updateFriendchatChannelSingleUser(message: UpdateFriendchatChannelSingleUser) {
        if (!filters[PropertyFilter.UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER]) return omit()
        root.string("type", if (message.rank == -128) "del" else "add")
        root.string("name", message.name)
        root.int("world", message.world)
        message.worldName?.let { root.string("worldname", it) }
        root.int("rank", message.rank)
        root.int("aliasflag", message.aliasFlag)
        message.alias?.let { root.string("alias", it) }
    }

    override fun telemetryGridValuesDelta(message: TelemetryGridValuesDelta) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.group("GROUPS") {
            for (entry in message.groups) {
                group("GROUP") {
                    int("index", entry.index)
                    group("ROWS") {
                        for (row in entry.rows) {
                            group("ROW") {
                                int("index", row.index)
                                group("CELLS") {
                                    for (cell in row.cells) {
                                        group("CELL") {
                                            int("column", cell.column)
                                            int("value", cell.value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun telemetryGridFull(message: TelemetryGridFull) {
        if (!filters[PropertyFilter.TELEMETRY_GRID]) return omit()
        root.group("GROUPS") {
            for (entry in message.groups) {
                group("GROUP") {
                    int("id", entry.id)
                    list("columns") {
                        for (id in entry.columnIds) int("", id)
                    }
                    group("ROWS") {
                        entry.rows.forEachIndexed { index, row ->
                            group("ROW") {
                                int("id", entry.rowIds[index])
                                int("pin", row.pin)
                                group("CELLS") {
                                    row.cells.forEachIndexed { column, cell ->
                                        group("CELL") {
                                            int("column", entry.columnIds[column])
                                            val value = cell.value
                                            if (value == null) any("value", "null") else int("value", value)
                                            filteredInt("present", cell.present, if (value == null) 0 else 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun locSelectAdd(message: LocSelectAdd) {
        if (!filters[PropertyFilter.LOC_SELECTION]) return omit()
        root.int("coordinate", message.coordinate)
        root.int("id", message.id)
        root.int("shaperotation", message.shapeRotation)
        message.transform?.let {
            root.children += AnyProperty("transform", it, LocSelectAdd.Transform::class.java)
        }
    }

    override fun rebuildRegion(message: RebuildRegion) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        root.int("zonex", message.baseChunkX)
        root.int("zonez", message.baseChunkZ)
        root.int("mode", message.mode)
        root.int("widthinzones", message.rows)
        root.int("lengthinzones", message.columns)
        root.sceneBase(message.baseTileX, message.baseTileZ)
        root.appendBuildArea(
            message,
            sessionState.getActiveWorld().instanceBounds,
            settingSetStore.getActive()[Setting.COLLAPSE_INSTANCE_ZONES],
        )
        root.group("DETAILS") {
            int("regionoriginx", message.regionOriginX)
            int("regionoriginz", message.regionOriginZ)
            int("format", message.format)
            int("npcscenevalue", message.npcSceneValue)
            filteredInt("unused", message.unused, 0)
        }
    }

    override fun consoleFeedback(message: ConsoleFeedback) {
        if (!filters[PropertyFilter.CONSOLE_FEEDBACK]) return omit()
        root.string("unusedtext", message.unusedText)
        root.string("prefix", message.prefix)
        root.int("totalmatches", message.totalMatches)
        root.int("resultcount", message.resultCount)
        root.children += AnyProperty("results", message.results, List::class.java)
    }

    override fun updateStockmarketSlotV2(message: UpdateStockmarketSlotV2) {
        if (!filters[PropertyFilter.UPDATE_STOCKMARKET_SLOT]) return omit()
        root.int("group", message.group)
        root.int("slot", message.slot)
        root.int("state", message.state)
        message.offer?.let { offer ->
            root.scriptVarType("id", ScriptVarType.OBJ, offer.objectId)
            root.formattedLong("price", offer.price)
            root.formattedInt("count", offer.quantity)
            root.formattedInt("completedcount", offer.completedQuantity)
            root.formattedLong("completedgold", offer.total)
            offer.version?.let { root.int("version", it) }
            offer.updatedState?.let { root.int("updatedstate", it) }
            offer.extensionLength?.let { root.int("extensionlength", it) }
            root.list("extension") {
                for (value in offer.extension) int("", value)
            }
        }
        root.list("reserved") {
            for (value in message.reserved) int("", value)
        }
    }

    override fun environmentOverride(message: EnvironmentOverride) {
        if (!filters[PropertyFilter.ENVIRONMENT_OVERRIDE]) return omit()
        root.long("flags", message.flags)
        root.children += AnyProperty("fields", message.fields, List::class.java)
        root.int("duration", message.duration)
    }

    override fun clanChannelFull(message: ClanChannelFull) {
        if (!filters[PropertyFilter.CLANCHANNEL]) return omit()
        root.int("clantype", message.channelIndex)
        val channel = message.channel ?: return
        root.group("DETAILS") {
            int("flags", channel.flags)
            int("version", channel.version)
            long("clanhash", channel.discardedKey)
            long("updatenum", channel.updateNumber)
            string("clanname", channel.name)
            boolean("discarded", channel.headerBoolean)
            int("kickrank", channel.kickRank)
            int("talkrank", channel.talkRank)
        }
        root.group("MEMBERS") {
            for (member in channel.members) {
                group {
                    string("name", member.name)
                    int("rank", member.rank)
                    int("world", member.world)
                    member.memberBoolean?.let { boolean("discarded", it) }
                }
            }
        }
    }

    override fun clanChannelDelta(message: ClanChannelDelta) {
        if (!filters[PropertyFilter.CLANCHANNEL]) return omit()
        root.int("clantype", message.channelIndex)
        root.long("clanhash", message.discardedKey)
        root.long("updatenum", message.updateNumber)
        // Preserve wire order, not the native application's reverse iteration.
        for (record in message.records) {
            when (record) {
                is ClanChannelDelta.Add -> root.group("ADD_USER") {
                    string("name", record.name)
                    int("world", record.world)
                    int("rank", record.rank)
                    long("memberidentity", record.memberIdentity)
                }
                is ClanChannelDelta.Remove -> root.group("DEL_USER") {
                    int("memberindex", record.index)
                    int("auxiliary", record.auxiliary)
                    int("sentinel", record.sentinel)
                }
                is ClanChannelDelta.Header -> root.group("UPDATE_BASE_SETTINGS") {
                    string("clanname", record.name)
                    int("talkrank", record.talkRank)
                    int("kickrank", record.kickRank)
                    boolean("discarded", record.headerBoolean)
                }
                is ClanChannelDelta.Member -> root.group("UPDATE_USER_DETAILS") {
                    int("memberindex", record.index)
                    string("name", record.name)
                    int("rank", record.rank)
                    int("world", record.world)
                    long("memberidentity", record.memberIdentity)
                    int("unused", record.unused)
                    boolean("discarded", record.memberBoolean)
                }
                is ClanChannelDelta.Rejected -> root.group("REJECTED") {
                    int("type", record.type)
                    int("sentinel", record.sentinel)
                }
            }
        }
        root.int("terminator", message.terminator)
    }

    override fun clanSettingsFull(message: ClanSettingsFull) {
        if (!filters[PropertyFilter.CLANSETTINGS]) return omit()
        root.int("clantype", message.channelIndex)
        val details = message.settings ?: return
        root.int("version", details.version)
        root.int("flags", details.flags)
        root.int("updatenum", details.updateNumber)
        root.int("creationtime", details.legacyTimestamp)
        root.string("clanname", details.name)
        root.boolean("allowunaffined", details.allowGuests)
        // RS3 rank slots are not all equivalent to OSRS's four rank fields.
        root.int("rank0", details.rank0)
        root.int("rank1", details.rank1)
        root.int("rank2", details.rank2)
        root.boolean("headerboolean", details.headerBoolean)
        details.extra?.let { root.int("extra", it) }
        root.group("AFFINED_MEMBERS") {
            for (member in details.members) {
                group {
                    string("name", member.name)
                    int("rank", member.rank)
                    member.bits?.let { int("extrainfo", it) }
                    member.joinedDay?.let { int("joinruneday", it) }
                    member.muted?.let { boolean("muted", it) }
                }
            }
        }
        root.group("BANNED_MEMBERS") {
            for (name in details.banned) group { string("name", name) }
        }
        root.group("SETTINGS") {
            for (parameter in details.parameters) {
                group {
                    int("id", parameter.key)
                    when (parameter) {
                        is ClanSettingsFull.IntParameter -> int("int", parameter.value)
                        is ClanSettingsFull.LongParameter -> long("long", parameter.value)
                        is ClanSettingsFull.StringParameter -> string("string", parameter.value)
                        is ClanSettingsFull.NullParameter -> boolean("null", true)
                    }
                }
            }
        }
    }

    override fun clanSettingsDelta(message: ClanSettingsDelta) {
        if (!filters[PropertyFilter.CLANSETTINGS]) return omit()
        root.int("clantype", message.channelIndex)
        root.long("ownerhash", message.discardedKey)
        root.int("updatenum", message.revision)
        root.group("UPDATES") {
            for (record in message.records) {
                when (record) {
                    is ClanSettingsDelta.AddName -> group(
                        when (record.type) {
                            1 -> "ADD_MEMBER_V1"
                            3 -> "ADD_BANNED"
                            13 -> "ADD_MEMBER_V2"
                            else -> error("Unknown add-name selector " + record.type)
                        },
                    ) {
                        string("name", record.name)
                        record.joinedDay?.let { int("joinruneday", it) }
                    }
                    is ClanSettingsDelta.Rank -> group("SET_MEMBER_RANK") {
                        int("memberindex", record.index)
                        int("rank", record.rank)
                    }
                    is ClanSettingsDelta.Permissions -> group("BASE_SETTINGS") {
                        boolean("allowunaffined", record.allowGuests)
                        int("rank0", record.rank0)
                        int("rank1", record.rank1)
                        int("rank2", record.rank2)
                        boolean("headerboolean", record.headerBoolean)
                    }
                    is ClanSettingsDelta.Remove -> group(
                        when (record.type) {
                            5 -> "DELETE_MEMBER"
                            6 -> "DELETE_BANNED"
                            else -> error("Unknown removal selector " + record.type)
                        },
                    ) { int("memberindex", record.index) }
                    is ClanSettingsDelta.MemberBits -> group("SET_MEMBER_EXTRA_INFO") {
                        int("memberindex", record.index)
                        int("value", record.value)
                        int("startbit", record.start)
                        int("endbit", record.end)
                    }
                    is ClanSettingsDelta.IntParameter -> group("SET_INT_SETTING") {
                        int("id", record.key)
                        int("value", record.value)
                    }
                    is ClanSettingsDelta.LongParameter -> group("SET_LONG_SETTING") {
                        int("id", record.key)
                        long("value", record.value)
                    }
                    is ClanSettingsDelta.StringParameter -> group("SET_STRING_SETTING") {
                        int("id", record.key)
                        string("value", record.value)
                    }
                    is ClanSettingsDelta.IntBits -> group("SET_VARBIT_SETTING") {
                        int("id", record.key)
                        int("value", record.value)
                        int("startbit", record.start)
                        int("endbit", record.end)
                    }
                    is ClanSettingsDelta.Name -> group("SET_CLAN_NAME") {
                        string("clanname", record.name)
                        int("extra", record.extra)
                    }
                    is ClanSettingsDelta.Mute -> group("SET_MEMBER_MUTED") {
                        int("memberindex", record.index)
                        boolean("muted", record.muted)
                    }
                    is ClanSettingsDelta.Rejected -> group("REJECTED") {
                        int("type", record.type)
                        int("sentinel", record.sentinel)
                    }
                }
            }
        }
        root.int("terminator", message.terminator)
    }

    override fun unknownServerOpcode(message: UnknownServerPacket) {
        if (!filters[PropertyFilter.UNKNOWN_SERVER_OPCODE_HEX]) return omit()
        if (message is RawUnknownServerPacket) {
            message.decodeFailure?.let { root.string("decodefailure", it) }
        }
        root.int("opcode", message.opcode)
        root.string("name", message.name)
        root.any("bytes", hex(message.bytes))
    }
}
