package net.rsprox.proxy.rs3.transcriber.interfaces

import net.rsprox.protocol.game.outgoing.model.misc.client.MinimapToggle
import net.rsprox.protocol.game.outgoing.model.misc.player.ChatFilterSettingsPrivateChat
import net.rsprox.protocol.game.outgoing.model.misc.player.RunClientScript
import net.rsprox.protocol.game.outgoing.model.unknown.UnknownServerPacket
import net.rsprox.protocol.rs3.game.outgoing.model.camera.*
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.*
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvStopTransmit
import net.rsprox.protocol.rs3.game.outgoing.model.map.RebuildNormal
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.Cutscene2dPlay
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintArrow
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.HintTrail
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.*
import net.rsprox.protocol.rs3.game.outgoing.model.sound.VorbisSound
import net.rsprox.protocol.rs3.game.outgoing.model.specific.ProjAnimSpecificV2
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitLarge
import net.rsprox.protocol.rs3.game.outgoing.model.varbit.VarbitSmall
import net.rsprox.protocol.rs3.game.outgoing.model.varc.*
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLarge
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpLong
import net.rsprox.protocol.rs3.game.outgoing.model.varp.VarpSmall
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZoneFullFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialEnclosed
import net.rsprox.protocol.rs3.game.outgoing.model.zone.header.UpdateZonePartialFollows
import net.rsprox.protocol.rs3.game.outgoing.model.zone.payload.*

public interface Rs3ServerPacketTranscriber {
    public fun varpSmall(message: VarpSmall)

    public fun varpLarge(message: VarpLarge)

    public fun varpLong(message: VarpLong)

    public fun varbitSmall(message: VarbitSmall)

    public fun varbitLarge(message: VarbitLarge)

    public fun ifOpenTop(message: IfOpenTop)

    public fun ifOpenSub(message: IfOpenSub)

    public fun ifCloseSub(message: IfCloseSub)

    public fun ifSetHide(message: IfSetHide)

    public fun messageGame(message: MessageGame)

    public fun rebuildNormal(message: RebuildNormal)

    public fun updateZoneFullFollows(message: UpdateZoneFullFollows)

    public fun updateZonePartialFollows(message: UpdateZonePartialFollows)

    public fun locAnim(message: LocAnim)

    public fun locAddChange(message: LocAddChange)

    public fun locDel(message: LocDel)

    public fun objAdd(message: ObjAdd)

    public fun objDel(message: ObjDel)

    public fun objCount(message: ObjCount)

    public fun objReveal(message: ObjReveal)

    public fun mapAnim(message: MapAnim)

    public fun mapAnimV2(message: MapAnimV2)

    public fun midiSongLocation(message: MidiSongLocation)

    public fun soundArea(message: SoundArea)

    public fun textCoord(message: TextCoord)

    public fun mapProjAnim(message: MapProjAnim)

    public fun mapProjAnimHalfsq(message: MapProjAnimHalfsq)

    public fun mapProjAnimHalfsqV2(message: MapProjAnimHalfsqV2)

    public fun mapProjAnimV2(message: MapProjAnimV2)

    public fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed)

    public fun varcSmall(message: VarcSmall)

    public fun varcLarge(message: VarcLarge)

    public fun varcBitSmall(message: VarcBitSmall)

    public fun varcBitLarge(message: VarcBitLarge)

    public fun varcStrSmall(message: VarcStrSmall)

    public fun ifSetNpcHead(message: IfSetNpcHead)

    public fun ifSetPlayerHead(message: IfSetPlayerHead)

    public fun ifSetText(message: IfSetText)

    public fun ifSetObject(message: IfSetObject)

    public fun ifOpenSubActiveObj(message: IfOpenSubActiveObj)

    public fun ifOpenSubActiveLoc(message: IfOpenSubActiveLoc)

    public fun ifSetModel(message: IfSetModel)

    public fun ifSetPosition(message: IfSetPosition)

    public fun ifSetAnim(message: IfSetAnim)

    public fun ifSetColour(message: IfSetColour)

    public fun ifSetScrollPos(message: IfSetScrollPos)

    public fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf)

    public fun ifSetTargetParam(message: IfSetTargetParam)

    public fun ifSetEvents(message: IfSetEvents)

    public fun camLookAt(message: CamLookAt)

    public fun camShake(message: CamShake)

    public fun camForceAngle(message: CamForceAngle)

    public fun camMoveTo(message: CamMoveTo)

    public fun cameraUpdate(message: CameraUpdate)

    public fun updateInvFull(message: UpdateInvFull)

    public fun updateInvStopTransmit(message: UpdateInvStopTransmit)

    public fun updateInvPartial(message: UpdateInvPartial)

    public fun updateRunWeight(message: UpdateRunWeight)

    public fun updateStat(message: UpdateStat)

    public fun updateRunEnergy(message: UpdateRunEnergy)

    public fun minimapToggle(message: MinimapToggle)

    public fun hintTrail(message: HintTrail)

    public fun hintArrow(message: HintArrow)

    public fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat)

    public fun setPlayerOp(message: SetPlayerOp)

    public fun ifSetPlayerModelSnapshot(message: IfSetPlayerModelSnapshot)

    public fun ifSetPlayerHeadSnapshot(message: IfSetPlayerHeadSnapshot)

    public fun vorbisSound(message: VorbisSound)

    public fun runClientScript(message: RunClientScript)

    public fun projAnimSpecificV2(message: ProjAnimSpecificV2)

    public fun locPrefetch(message: LocPrefetch)

    public fun cutscene2dPlay(message: Cutscene2dPlay)

    public fun jcoinsUpdate(message: JcoinsUpdate)

    public fun unknownServerOpcode(message: UnknownServerPacket)
}
