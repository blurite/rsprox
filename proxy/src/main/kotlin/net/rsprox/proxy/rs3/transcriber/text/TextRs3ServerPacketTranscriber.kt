package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage
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
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.proxy.rs3.transcriber.interfaces.Rs3ServerPacketTranscriber
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.group
import net.rsprox.shared.property.regular.AnyProperty

public class TextRs3ServerPacketTranscriber(
    private val sessionState: Rs3SessionState,
    private val filterSetStore: PropertyFilterSetStore,
) : Rs3ServerPacketTranscriber {
    private val root: RootProperty
        get() = checkNotNull(sessionState.root.lastOrNull()) {
            "No active root - onTranscribeStart() must run before dispatching to a transcriber method"
        }
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    private fun formatCoord(c: CoordGrid): String = "(${c.level},${c.x},${c.z})"

    private fun hex(bytes: ByteArray): String = bytes.joinToString(" ") { "%02x".format(it) }

    private fun skillLabel(id: Int): String {
        val name = SKILL_NAMES.getOrNull(id) ?: "?"
        return "$name($id)"
    }

    private companion object {
        private val SKILL_NAMES =
            arrayOf(
                "attack", "defence", "strength", "constitution", "ranged", "prayer", "magic", "cooking",
                "woodcutting", "fletching", "fishing", "firemaking", "crafting", "smithing", "mining",
                "herblore", "agility", "thieving", "slayer", "farming", "runecrafting", "hunter",
                "construction", "summoning", "dungeoneering", "divination", "invention", "archaeology",
                "necromancy",
            )
    }

    override fun varpSmall(message: VarpSmall) {
        if (!filters[PropertyFilter.VARP]) return omit()
        root.children += AnyProperty("varp", Rs3GamevalLookup.varp(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varpLarge(message: VarpLarge) {
        if (!filters[PropertyFilter.VARP]) return omit()
        root.children += AnyProperty("varp", Rs3GamevalLookup.varp(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varpLong(message: VarpLong) {
        if (!filters[PropertyFilter.VARP]) return omit()
        root.children += AnyProperty("varp", Rs3GamevalLookup.varp(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Long::class.java)
    }

    override fun varbitSmall(message: VarbitSmall) {
        if (!filters[PropertyFilter.VARBITS]) return omit()
        root.children += AnyProperty("varbit", Rs3GamevalLookup.varbit(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varbitLarge(message: VarbitLarge) {
        if (!filters[PropertyFilter.VARBITS]) return omit()
        root.children += AnyProperty("varbit", Rs3GamevalLookup.varbit(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun ifOpenTop(message: IfOpenTop) {
        if (!filters[PropertyFilter.IF_OPENTOP]) return omit()
        val existing = sessionState.toplevelInterface
        if (existing != -1) {
            root.children += AnyProperty("previousid", Rs3GamevalLookup.interfaceName(existing), String::class.java)
        }
        root.children += AnyProperty("interface", Rs3GamevalLookup.interfaceName(message.interfaceId), String::class.java)
    }

    override fun ifOpenSub(message: IfOpenSub) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("child", Rs3GamevalLookup.interfaceName(message.childId), String::class.java)
        root.children += AnyProperty("layer", message.layer, Int::class.java)
    }

    override fun ifCloseSub(message: IfCloseSub) {
        if (!filters[PropertyFilter.IF_CLOSESUB]) return omit()
        root.children +=
            AnyProperty(
                "parentComponent",
                Rs3GamevalLookup.component(message.parentComponentHash),
                String::class.java,
            )
        val interfaceId = sessionState.getOpenInterface(message.parentComponentHash)
        if (interfaceId != null) {
            root.children += AnyProperty("interface", Rs3GamevalLookup.interfaceName(interfaceId), String::class.java)
        }
    }

    override fun ifSetHide(message: IfSetHide) {
        if (!filters[PropertyFilter.IF_SETHIDE]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("hide", message.hidden, Boolean::class.java)
    }

    override fun messageGame(message: MessageGame) {
        if (!filters[PropertyFilter.MESSAGE_GAME]) return omit()
        root.children += AnyProperty("type", message.type, Int::class.java)
        if (message.effectFlags != 0) {
            root.children += AnyProperty("flags", message.effectFlags, Int::class.java)
        }
        val name = message.name
        if (name != null) {
            root.children += AnyProperty("name", name, String::class.java)
        }
        root.children += AnyProperty("message", message.message, String::class.java)
    }

    override fun rebuildNormal(message: RebuildNormal) {
        if (!filters[PropertyFilter.REBUILD]) return omit()
        val initBlock = message.playerInfoInitBlock
        if (initBlock != null) {
            root.children += AnyProperty(
                "self",
                "(${initBlock.localPlayerLevel},${initBlock.localPlayerX},${initBlock.localPlayerZ})",
                String::class.java,
            )
            root.children += AnyProperty("nonZeroPositionCount", initBlock.nonZeroPositionCount, Int::class.java)
        } else {
            root.children += AnyProperty("gpiTooShort", true, Boolean::class.java)
        }
        if (!message.trailerAligned) {
            root.children += AnyProperty("trailerMisaligned", true, Boolean::class.java)
            return
        }
        root.children += AnyProperty("worldAreaTypeId", message.worldAreaTypeId, Int::class.java)
        root.children += AnyProperty("baseTile", "(0,${message.baseTileX},${message.baseTileZ})", String::class.java)
    }

    private fun Property.buildZoneFollowsCommon(level: Int, zoneX: Int, zoneZ: Int) {
        children += AnyProperty("level", level, Int::class.java)
        children += AnyProperty("zoneX", zoneX, Int::class.java)
        children += AnyProperty("zoneZ", zoneZ, Int::class.java)
        val base = sessionState.getActiveWorld().relativizeZoneCoord(0, 0)
        children += AnyProperty("zoneBase", formatCoord(base), String::class.java)
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
        children += AnyProperty("loc", Rs3GamevalLookup.loc(event.id), String::class.java)
        children += AnyProperty("shape", event.shape, Int::class.java)
        children += AnyProperty("rotation", event.rotation, Int::class.java)
        children += AnyProperty("rawBytes", hex(event.rawBytes), String::class.java)
    }

    private fun Property.buildLocCustomise(event: LocCustomise) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("loc", Rs3GamevalLookup.loc(event.locId), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("shape", event.shape, Int::class.java)
        children += AnyProperty("rotation", event.rotation, Int::class.java)
        if (event.hasExtendedTransform) {
            children += AnyProperty("rotationX", event.rotationX, Float::class.java)
            children += AnyProperty("rotationY", event.rotationY, Float::class.java)
            children += AnyProperty("rotationZ", event.rotationZ, Float::class.java)
            children += AnyProperty("rotationW", event.rotationW, Float::class.java)
            children += AnyProperty("translateA", event.translateA, Float::class.java)
            children += AnyProperty("translateB", event.translateB, Float::class.java)
            children += AnyProperty("translateC", event.translateC, Float::class.java)
            children += AnyProperty("scaleX", event.scaleX, Float::class.java)
            children += AnyProperty("scaleY", event.scaleY, Float::class.java)
            children += AnyProperty("scaleZ", event.scaleZ, Float::class.java)
        }
        event.uintArray?.let {
            children += AnyProperty("uintArray", it.joinToString(","), String::class.java)
        }
        event.opcodeArrayA?.let {
            children += AnyProperty("opcodeArrayA", it.joinToString(","), String::class.java)
        }
        event.opcodeArrayB?.let {
            children += AnyProperty("opcodeArrayB", it.joinToString(","), String::class.java)
        }
        children += AnyProperty("rawBytes", hex(event.rawBytes), String::class.java)
    }

    private fun Property.buildLocAnim(event: LocAnim) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("anim", Rs3GamevalLookup.seq(event.id), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("shape", event.shape, Int::class.java)
        children += AnyProperty("rotation", event.rotation, Int::class.java)
        children += AnyProperty("delay?", event.delay, Int::class.java)
    }

    override fun locAnim(message: LocAnim) {
        if (!filters[PropertyFilter.LOC_ANIM]) return omit()
        root.buildLocAnim(message)
    }

    private fun Property.buildLocAddChange(event: LocAddChange) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("loc", Rs3GamevalLookup.loc(event.locId), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("shape", event.shape, Int::class.java)
        children += AnyProperty("rotation", event.rotation, Int::class.java)
    }

    override fun locAddChange(message: LocAddChange) {
        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) return omit()
        root.buildLocAddChange(message)
    }

    private fun Property.buildMidiSongLocation(event: MidiSongLocation) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("song", Rs3GamevalLookup.midi(event.id), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("maxDistance", event.maxDistance, Int::class.java)
        children += AnyProperty("minDistance", event.minDistance, Int::class.java)
        children += AnyProperty("volume", event.volume, Int::class.java)
    }

    override fun midiSongLocation(message: MidiSongLocation) {
        if (!filters[PropertyFilter.MIDI_SONG]) return omit()
        root.buildMidiSongLocation(message)
    }

    private fun Property.buildLocDel(event: LocDel) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("shape", event.shape, Int::class.java)
        children += AnyProperty("rotation", event.rotation, Int::class.java)
    }

    override fun locDel(message: LocDel) {
        if (!filters[PropertyFilter.LOC_DEL]) return omit()
        root.buildLocDel(message)
    }

    private fun Property.buildObjAdd(event: ObjAdd) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("obj", Rs3GamevalLookup.obj(event.objId), String::class.java)
        children += AnyProperty("count", event.count, Int::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
    }

    override fun objAdd(message: ObjAdd) {
        if (!filters[PropertyFilter.OBJ_ADD]) return omit()
        root.buildObjAdd(message)
    }

    private fun Property.buildObjDel(event: ObjDel) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("obj", Rs3GamevalLookup.obj(event.objId), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
    }

    override fun objDel(message: ObjDel) {
        if (!filters[PropertyFilter.OBJ_DEL]) return omit()
        root.buildObjDel(message)
    }

    private fun Property.buildObjCount(event: ObjCount) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("obj", Rs3GamevalLookup.obj(event.objId), String::class.java)
        children += AnyProperty("oldQuantity", event.oldQuantity, Int::class.java)
        children += AnyProperty("newQuantity", event.newQuantity, Int::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
    }

    override fun objCount(message: ObjCount) {
        if (!filters[PropertyFilter.OBJ_COUNT]) return omit()
        root.buildObjCount(message)
    }

    private fun Property.buildObjReveal(event: ObjReveal) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("obj", Rs3GamevalLookup.obj(event.objId), String::class.java)
        children += AnyProperty("count", event.count, Int::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        val ownerName = sessionState.getPlayerOrNull(event.ownerIndex)?.name
        val ownerLabel = if (ownerName != null) "$ownerName(${event.ownerIndex})" else "${event.ownerIndex}"
        children += AnyProperty("owner", ownerLabel, String::class.java)
        children += AnyProperty("rawBytes", hex(event.rawBytes), String::class.java)
    }

    override fun objReveal(message: ObjReveal) {
        if (!filters[PropertyFilter.OBJ_ADD]) return omit()
        root.buildObjReveal(message)
    }

    private fun Property.buildMapAnim(event: MapAnim) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("sound", Rs3GamevalLookup.sound(event.id), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("height", event.height, Int::class.java)
        children += AnyProperty("delay", event.delay, Int::class.java)
        children += AnyProperty("rotation", event.rotation, Int::class.java)
    }

    override fun mapAnim(message: MapAnim) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildMapAnim(message)
    }

    private fun Property.buildMapAnimV2(event: MapAnimV2) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("id", event.id, Int::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("heightByte", event.heightByte, Int::class.java)
        children += AnyProperty("fineOffsetPacked", event.fineOffsetPacked, Int::class.java)
        children += AnyProperty("val2", event.val2, Int::class.java)
        children += AnyProperty("val3", event.val3, Int::class.java)
        children += AnyProperty("rawBytes", hex(event.rawBytes), String::class.java)
    }

    override fun mapAnimV2(message: MapAnimV2) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildMapAnimV2(message)
    }

    private fun Property.buildSoundArea(event: SoundArea) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("sound", Rs3GamevalLookup.sound(event.soundId), String::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("loopCount", event.loopCount, Int::class.java)
        children += AnyProperty("range", event.range, Int::class.java)
        if (event.rotation != 0) children += AnyProperty("rotation", event.rotation, Int::class.java)
        if (event.heightOffset != 0) children += AnyProperty("heightOffset", event.heightOffset, Int::class.java)
    }

    override fun soundArea(message: SoundArea) {
        if (!filters[PropertyFilter.SOUND_AREA]) return omit()
        root.buildSoundArea(message)
    }

    private fun Property.buildTextCoord(event: TextCoord) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("height", event.height, Int::class.java)
        children += AnyProperty("duration", event.duration, Int::class.java)
        children += AnyProperty("rgb", "#%06x".format(event.rgb), String::class.java)
        children += AnyProperty("text", event.text, String::class.java)
    }

    override fun textCoord(message: TextCoord) {
        if (!filters[PropertyFilter.MAP_ANIM]) return omit()
        root.buildTextCoord(message)
    }

    private fun Property.buildMapProjAnim(event: MapProjAnim) {
        val world = sessionState.getActiveWorld()
        val c = world.relativizeZoneCoord(event.xInZone, event.zInZone)
        val dest = world.relativizeZoneCoord(event.xInZone + event.targetDeltaX, event.zInZone + event.targetDeltaY)
        children += AnyProperty("id", event.id, Int::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("destCoord", formatCoord(dest), String::class.java)
        children += AnyProperty("startHeight", event.startHeight, Int::class.java)
        children += AnyProperty("endHeight", event.endHeight, Int::class.java)
        children += AnyProperty("startTime", event.startTime, Int::class.java)
        children += AnyProperty("endTime", event.endTime, Int::class.java)
        if (event.alpha != 0) children += AnyProperty("alpha", event.alpha, Int::class.java)
        if (event.lockonSlot != 0) children += AnyProperty("lockonSlot", event.lockonSlot, Int::class.java)
    }

    override fun mapProjAnim(message: MapProjAnim) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnim(message)
    }

    private fun Property.buildMapProjAnimHalfsq(event: MapProjAnimHalfsq) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("id", event.id, Int::class.java)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("destXdeltaHalf", event.destXdeltaHalf, Int::class.java)
        children += AnyProperty("destYdeltaHalf", event.destYdeltaHalf, Int::class.java)
        if (event.trailingBytes.isNotEmpty()) {
            children += AnyProperty("trailingBytes", hex(event.trailingBytes), String::class.java)
        }
        children += AnyProperty("rawBytes", hex(event.rawBytes), String::class.java)
    }

    override fun mapProjAnimHalfsq(message: MapProjAnimHalfsq) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnimHalfsq(message)
    }

    private fun Property.buildMapProjAnimHalfsqV2(event: MapProjAnimHalfsqV2) {
        val origin = sessionState.getActiveWorld().relativizeZoneCoord(0, 0)
        val startHalfX = origin.x * 2 + event.xInZoneHalf
        val startHalfZ = origin.z * 2 + event.zInZoneHalf
        val destHalfX = startHalfX + event.destXdeltaHalf
        val destHalfZ = startHalfZ + event.destYdeltaHalf

        children += AnyProperty("spotAnimId", event.spotAnimId, Int::class.java)
        children += AnyProperty("startCoord", formatHalfCoord(origin.level, startHalfX, startHalfZ), String::class.java)
        children += AnyProperty("destCoord", formatHalfCoord(origin.level, destHalfX, destHalfZ), String::class.java)
        children += AnyProperty("source", formatEntityRef(event.sourceType, event.sourceIndex), String::class.java)
        children += AnyProperty("target", formatEntityRef(event.targetType, event.targetIndex), String::class.java)
        children += AnyProperty("startHeight", event.startHeight, Int::class.java)
        children += AnyProperty("endHeight", event.endHeight, Int::class.java)
        children += AnyProperty("startTime", event.startTime, Int::class.java)
        children += AnyProperty("endTime", event.endTime, Int::class.java)
        if (event.alpha != 0) children += AnyProperty("alpha?", event.alpha, Int::class.java)
        children += AnyProperty("angle?", event.angle, Int::class.java)
        children += AnyProperty("flags?", event.flags, Int::class.java)
        children += AnyProperty("startOffset?", "0x%06x".format(event.startOffset), String::class.java)
        children += AnyProperty("endOffset?", "0x%06x".format(event.endOffset), String::class.java)
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

    private fun formatHalfCoord(
        level: Int,
        halfX: Int,
        halfZ: Int,
    ): String {
        val x = halfX shr 1
        val z = halfZ shr 1
        val xFrac = if (halfX and 1 != 0) ".5" else ""
        val zFrac = if (halfZ and 1 != 0) ".5" else ""
        return "($level,$x$xFrac,$z$zFrac)"
    }


    override fun mapProjAnimHalfsqV2(message: MapProjAnimHalfsqV2) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        root.buildMapProjAnimHalfsqV2(message)
    }

    override fun mapProjAnimV2(message: MapProjAnimV2) {
        if (!filters[PropertyFilter.MAP_PROJANIM]) return omit()
        val c = sessionState.getActiveWorld().relativizeZoneCoord(message.xInZone, message.zInZone)
        root.children += AnyProperty("id", message.id, Int::class.java)
        root.children += AnyProperty("coord", formatCoord(c), String::class.java)
        root.children += AnyProperty("targetDeltaX", message.targetDeltaX, Int::class.java)
        root.children += AnyProperty("targetDeltaY", message.targetDeltaY, Int::class.java)
        if (message.trailingBytes.isNotEmpty()) {
            root.children += AnyProperty("trailingBytes", hex(message.trailingBytes), String::class.java)
        }
        root.children += AnyProperty("rawBytes", hex(message.rawBytes), String::class.java)
    }


    override fun updateZonePartialEnclosed(message: UpdateZonePartialEnclosed) {
        val includeZoneHeader = filters[PropertyFilter.ZONE_HEADER]
        if (includeZoneHeader) {
            root.children += AnyProperty("level", message.level, Int::class.java)
            root.children += AnyProperty("zoneX", message.zoneX, Int::class.java)
            root.children += AnyProperty("zoneZ", message.zoneZ, Int::class.java)
            root.children += AnyProperty("packetCount", message.packets.size, Int::class.java)
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
                is MidiSongLocation -> {
                    if (!filters[PropertyFilter.MIDI_SONG]) continue
                    root.group("MIDI_SONG_LOCATION") { buildMidiSongLocation(event) }
                }
                is LocDel -> {
                    if (!filters[PropertyFilter.LOC_DEL]) continue
                    root.group("LOC_DEL") { buildLocDel(event) }
                }
                is ObjAdd -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    root.group("OBJ_ADD") { buildObjAdd(event) }
                }
                is ObjDel -> {
                    if (!filters[PropertyFilter.OBJ_DEL]) continue
                    root.group("OBJ_DEL") { buildObjDel(event) }
                }
                is ObjCount -> {
                    if (!filters[PropertyFilter.OBJ_COUNT]) continue
                    root.group("OBJ_COUNT") { buildObjCount(event) }
                }
                is ObjReveal -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    root.group("OBJ_REVEAL") { buildObjReveal(event) }
                }
                is MapAnim -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("MAP_ANIM(sound?)") { buildMapAnim(event) }
                }
                is MapAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("MAP_ANIM_V2") { buildMapAnimV2(event) }
                }
                is SoundArea -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    root.group("SOUND_AREA") { buildSoundArea(event) }
                }
                is TextCoord -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    root.group("TEXT_COORD") { buildTextCoord(event) }
                }
                is MapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM") { buildMapProjAnim(event) }
                }
                is MapProjAnimHalfsq -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_HALFSQ") { buildMapProjAnimHalfsq(event) }
                }
                is MapProjAnimHalfsqV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_HALFSQ_V2") { buildMapProjAnimHalfsqV2(event) }
                }
                is MapProjAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    root.group("MAP_PROJANIM_V2") { buildMapProjAnimV2(event) }
                }
                else -> Unit
            }
        }
    }

    private fun createFakeZoneProts(packets: List<IncomingServerGameMessage>) {
        for (event in packets) {
            when (event) {
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
                    sessionState.createFakeServerRoot("LOC_CUSTOMISE?").buildLocCustomise(event)
                }
                is LocPrefetch -> {
                    if (!filters[PropertyFilter.LOC_ADD_CHANGE]) continue
                    sessionState.createFakeServerRoot("LOC_PREFETCH").buildLocPrefetch(event)
                }
                is MidiSongLocation -> {
                    if (!filters[PropertyFilter.MIDI_SONG]) continue
                    sessionState.createFakeServerRoot("MIDI_SONG_LOCATION").buildMidiSongLocation(event)
                }
                is LocDel -> {
                    if (!filters[PropertyFilter.LOC_DEL]) continue
                    sessionState.createFakeServerRoot("LOC_DEL").buildLocDel(event)
                }
                is ObjAdd -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    sessionState.createFakeServerRoot("OBJ_ADD").buildObjAdd(event)
                }
                is ObjDel -> {
                    if (!filters[PropertyFilter.OBJ_DEL]) continue
                    sessionState.createFakeServerRoot("OBJ_DEL").buildObjDel(event)
                }
                is ObjCount -> {
                    if (!filters[PropertyFilter.OBJ_COUNT]) continue
                    sessionState.createFakeServerRoot("OBJ_COUNT").buildObjCount(event)
                }
                is ObjReveal -> {
                    if (!filters[PropertyFilter.OBJ_ADD]) continue
                    sessionState.createFakeServerRoot("OBJ_REVEAL").buildObjReveal(event)
                }
                is MapAnim -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("MAP_ANIM(sound?)").buildMapAnim(event)
                }
                is MapAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("MAP_ANIM_V2").buildMapAnimV2(event)
                }
                is SoundArea -> {
                    if (!filters[PropertyFilter.SOUND_AREA]) continue
                    sessionState.createFakeServerRoot("SOUND_AREA").buildSoundArea(event)
                }
                is TextCoord -> {
                    if (!filters[PropertyFilter.MAP_ANIM]) continue
                    sessionState.createFakeServerRoot("TEXT_COORD").buildTextCoord(event)
                }
                is MapProjAnim -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM").buildMapProjAnim(event)
                }
                is MapProjAnimHalfsq -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_HALFSQ").buildMapProjAnimHalfsq(event)
                }
                is MapProjAnimHalfsqV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_HALFSQ_V2").buildMapProjAnimHalfsqV2(event)
                }
                is MapProjAnimV2 -> {
                    if (!filters[PropertyFilter.MAP_PROJANIM]) continue
                    sessionState.createFakeServerRoot("MAP_PROJANIM_V2").buildMapProjAnimV2(event)
                }
                else -> Unit
            }
        }
    }

    private fun Property.buildMapProjAnimV2(event: MapProjAnimV2) {
        val c = sessionState.getActiveWorld().relativizeZoneCoord(event.xInZone, event.zInZone)
        children += AnyProperty("coord", formatCoord(c), String::class.java)
        children += AnyProperty("targetDeltaX", event.targetDeltaX, Int::class.java)
        children += AnyProperty("targetDeltaY", event.targetDeltaY, Int::class.java)
        children += AnyProperty("idMedium", event.idMedium, Int::class.java)
        children += AnyProperty("id", event.id, Int::class.java)
        children += AnyProperty("trailingBytes", hex(event.trailingBytes), String::class.java)
        children += AnyProperty("rawBytes", hex(event.rawBytes), String::class.java)
    }

    override fun varcSmall(message: VarcSmall) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.children += AnyProperty("varc", Rs3GamevalLookup.varc(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varcLarge(message: VarcLarge) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.children += AnyProperty("varc", Rs3GamevalLookup.varc(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varcBitSmall(message: VarcBitSmall) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.children += AnyProperty("varcbit", Rs3GamevalLookup.varc(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varcBitLarge(message: VarcBitLarge) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.children += AnyProperty("varcbit", Rs3GamevalLookup.varc(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, Int::class.java)
    }

    override fun varcStrSmall(message: VarcStrSmall) {
        if (!filters[PropertyFilter.VARC]) return omit()
        root.children += AnyProperty("varc", Rs3GamevalLookup.varc(message.id), String::class.java)
        root.children += AnyProperty("value", message.value, String::class.java)
    }

    override fun ifSetNpcHead(message: IfSetNpcHead) {
        if (!filters[PropertyFilter.IF_SETNPCHEAD]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("npc", Rs3GamevalLookup.npc(message.npcId), String::class.java)
    }

    override fun ifSetPlayerHead(message: IfSetPlayerHead) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
    }

    override fun ifSetText(message: IfSetText) {
        if (!filters[PropertyFilter.IF_SETTEXT]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("text", message.text, String::class.java)
    }

    override fun ifSetObject(message: IfSetObject) {
        if (!filters[PropertyFilter.IF_SETOBJECT]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("obj", Rs3GamevalLookup.obj(message.objId), String::class.java)
        root.children += AnyProperty("count", message.count, Int::class.java)
    }

    override fun ifOpenSubActiveObj(message: IfOpenSubActiveObj) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("child", Rs3GamevalLookup.interfaceName(message.childId), String::class.java)
        root.children += AnyProperty("obj", Rs3GamevalLookup.obj(message.objId), String::class.java)
        root.children += AnyProperty("layer", message.layer, Int::class.java)
    }

    override fun ifOpenSubActiveLoc(message: IfOpenSubActiveLoc) {
        if (!filters[PropertyFilter.IF_OPENSUB]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("child", Rs3GamevalLookup.interfaceName(message.childId), String::class.java)
        root.children += AnyProperty("loc", Rs3GamevalLookup.loc(message.locId), String::class.java)
        root.children += AnyProperty("shape", message.shape, Int::class.java)
        root.children += AnyProperty("rotation", message.rotation, Int::class.java)
        root.children += AnyProperty("layer", message.layer, Int::class.java)
    }

    override fun ifSetModel(message: IfSetModel) {
        if (!filters[PropertyFilter.IF_SETMODEL]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("model", Rs3GamevalLookup.model(message.modelId), String::class.java)
    }

    override fun ifSetPosition(message: IfSetPosition) {
        if (!filters[PropertyFilter.IF_SETPOSITION]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("x", message.x, Int::class.java)
        root.children += AnyProperty("y", message.y, Int::class.java)
    }

    override fun ifSetAnim(message: IfSetAnim) {
        if (!filters[PropertyFilter.IF_SETANIM]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("anim", Rs3GamevalLookup.seq(message.animId), String::class.java)
    }

    override fun ifSetColour(message: IfSetColour) {
        if (!filters[PropertyFilter.IF_SETCOLOUR]) return omit()
        val colorRaw = message.packedColor
        val r5 = (colorRaw ushr 10) and 0x1F
        val g5 = (colorRaw ushr 5) and 0x1F
        val b5 = colorRaw and 0x1F
        val r8 = (r5 shl 3) or (r5 ushr 2)
        val g8 = (g5 shl 3) or (g5 ushr 2)
        val b8 = (b5 shl 3) or (b5 ushr 2)
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("rgb", "#%02x%02x%02x".format(r8, g8, b8), String::class.java)
    }

    override fun ifSetScrollPos(message: IfSetScrollPos) {
        if (!filters[PropertyFilter.IF_SETSCROLLPOS]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("scrollPos", message.scrollPos, Int::class.java)
    }

    override fun ifSetPlayerModelSelf(message: IfSetPlayerModelSelf) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
    }

    override fun ifSetTargetParam(message: IfSetTargetParam) {
        if (!filters[PropertyFilter.IF_SETEVENTS]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("targetParam", message.targetParam, Int::class.java)
        val fromStr = if (message.fromSlot == 65535) "-1" else message.fromSlot.toString()
        val toStr = if (message.toSlot == 65535) "-1" else message.toSlot.toString()
        root.children += AnyProperty("fromSlot", fromStr, String::class.java)
        root.children += AnyProperty("toSlot", toStr, String::class.java)
    }

    override fun ifSetEvents(message: IfSetEvents) {
        if (!filters[PropertyFilter.IF_SETEVENTS]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("settings", message.settings, Int::class.java)
        root.children += AnyProperty("fromSlot", message.fromSlot, Int::class.java)
        root.children += AnyProperty("toSlot", message.toSlot, Int::class.java)
    }

    override fun camLookAt(message: CamLookAt) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.children += AnyProperty("localX", message.localX, Int::class.java)
        root.children += AnyProperty("localZ", message.localZ, Int::class.java)
        root.children += AnyProperty("height", message.height, Int::class.java)
        root.children += AnyProperty("speed", message.speed, Int::class.java)
        root.children += AnyProperty("accel", message.accel, Int::class.java)
    }

    override fun camShake(message: CamShake) {
        if (!filters[PropertyFilter.CAM_SHAKE]) return omit()
        root.children += AnyProperty("shakeMode", message.shakeMode, Int::class.java)
        root.children += AnyProperty("param0", message.param0, Int::class.java)
        root.children += AnyProperty("param1", message.param1, Int::class.java)
        root.children += AnyProperty("param2", message.param2, Int::class.java)
        root.children += AnyProperty("param3", message.param3, Int::class.java)
    }

    override fun camForceAngle(message: CamForceAngle) {
        if (!filters[PropertyFilter.CAM_LOOKAT]) return omit()
        root.children += AnyProperty("yaw", message.yaw, Int::class.java)
        root.children += AnyProperty("pitch", message.pitch, Int::class.java)
    }

    override fun camMoveTo(message: CamMoveTo) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        root.children += AnyProperty("localX", message.localX, Int::class.java)
        root.children += AnyProperty("localZ", message.localZ, Int::class.java)
        root.children += AnyProperty("height", message.height, Int::class.java)
        root.children += AnyProperty("speed", message.speed, Int::class.java)
        root.children += AnyProperty("accel", message.accel, Int::class.java)
    }

    override fun cameraUpdate(message: CameraUpdate) {
        if (!filters[PropertyFilter.CAM_MOVETO]) return omit()
        root.children += AnyProperty("headerFlags", "0x${message.headerFlags.toString(16)}", String::class.java)
        root.children += AnyProperty("bitmask", "0x${message.bitmask.toString(16)}", String::class.java)
    }

    override fun updateInvFull(message: UpdateInvFull) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.children += AnyProperty("inv", Rs3GamevalLookup.inv(message.inventoryId), String::class.java)
        root.children += AnyProperty("flags", message.flags, Int::class.java)
        root.children += AnyProperty("slots", message.objs.size, Int::class.java)
        for ((slot, obj) in message.objs.withIndex()) {
            if (obj.id < 0) continue
            val varStr = if (obj.vars.isNotEmpty()) " vars=${obj.vars.joinToString(",") { "${it.varId}=${it.value}" }}" else ""
            root.children += AnyProperty("[$slot]", "${Rs3GamevalLookup.obj(obj.id)} x${obj.count}$varStr", String::class.java)
        }
    }

    override fun updateInvStopTransmit(message: UpdateInvStopTransmit) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.children += AnyProperty("inv", Rs3GamevalLookup.inv(message.inventoryId), String::class.java)
        root.children += AnyProperty("flags", message.flags, Int::class.java)
    }

    override fun updateInvPartial(message: UpdateInvPartial) {
        if (!filters[PropertyFilter.UPDATE_INV]) return omit()
        root.children += AnyProperty("inv", Rs3GamevalLookup.inv(message.inventoryId), String::class.java)
        root.children += AnyProperty("flags", message.flags, Int::class.java)
        for (obj in message.objs) {
            val label =
                if (obj.id < 0) {
                    "EMPTY"
                } else {
                    val varStr = if (obj.vars.isNotEmpty()) " vars=${obj.vars.joinToString(",") { "${it.varId}=${it.value}" }}" else ""
                    "${Rs3GamevalLookup.obj(obj.id)} x${obj.count}$varStr"
                }
            root.children += AnyProperty("[${obj.slot}]", label, String::class.java)
        }
    }

    override fun updateRunWeight(message: UpdateRunWeight) {
        if (!filters[PropertyFilter.UPDATE_RUNWEIGHT]) return omit()
        root.children += AnyProperty("weight", "${message.weight}kg", String::class.java)
    }

    override fun updateRunEnergy(message: UpdateRunEnergy) {
        if (!filters[PropertyFilter.UPDATE_RUNENERGY]) return omit()
        root.children += AnyProperty("energy", message.energy, Int::class.java)
    }

    override fun updateStat(message: UpdateStat) {
        if (!filters[PropertyFilter.UPDATE_STAT]) return omit()
        val gained = sessionState.getExperience(message.skillId)?.let { message.xp - it }
        root.children += AnyProperty("skill", skillLabel(message.skillId), String::class.java)
        root.children += AnyProperty("level", message.level, Int::class.java)
        root.children += AnyProperty("xp", message.xp, Int::class.java)
        if (gained != null) {
            root.children += AnyProperty("xpGained", gained, Long::class.java)
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
        root.children += AnyProperty("state", "$modeName($state)", String::class.java)
        root.children += AnyProperty("interactive", state < 3, Boolean::class.java)
    }

    override fun hintTrail(message: HintTrail) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        root.children += AnyProperty("slot", message.slot, Int::class.java)
        root.children += AnyProperty("model", Rs3GamevalLookup.model(message.modelId), String::class.java)
    }

    override fun hintArrow(message: HintArrow) {
        if (!filters[PropertyFilter.HINT_ARROW]) return omit()
        root.children += AnyProperty("slot", message.slot, Int::class.java)
        if (message.isReset) {
            root.children += AnyProperty("type", "RESET", String::class.java)
            return
        }
        root.children += AnyProperty("type", message.type, Int::class.java)
        root.children += AnyProperty("targetIndex", message.targetIndex ?: -1, Int::class.java)
        root.children += AnyProperty("x", message.x ?: -1, Int::class.java)
        root.children += AnyProperty("y", message.y ?: -1, Int::class.java)
        root.children += AnyProperty("z", message.z ?: -1, Int::class.java)
        root.children += AnyProperty("distance", message.distance ?: -1, Int::class.java)
    }

    override fun chatFilterSettingsPrivateChat(message: ChatFilterSettingsPrivateChat) {
        if (!filters[PropertyFilter.CHAT_FILTER_SETTINGS]) return omit()
        root.children += AnyProperty("chatFilter", message.privateChatFilter, Int::class.java)
    }

    override fun setPlayerOp(message: SetPlayerOp) {
        if (!filters[PropertyFilter.SET_PLAYER_OP]) return omit()
        root.children += AnyProperty("slot", message.slot, Int::class.java)
        root.children += AnyProperty("text", message.text, String::class.java)
        root.children += AnyProperty("priority", message.priority, Boolean::class.java)
        root.children += AnyProperty("worldId", message.worldId, Int::class.java)
    }

    override fun ifSetPlayerModelSnapshot(message: IfSetPlayerModelSnapshot) {
        if (!filters[PropertyFilter.IF_SETPLAYERMODEL]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("snapshotSlot", message.snapshotSlot, Int::class.java)
    }

    override fun ifSetPlayerHeadSnapshot(message: IfSetPlayerHeadSnapshot) {
        if (!filters[PropertyFilter.IF_SETPLAYERHEAD]) return omit()
        root.children += AnyProperty("component", Rs3GamevalLookup.component(message.componentHash), String::class.java)
        root.children += AnyProperty("snapshotSlot", message.snapshotSlot, Int::class.java)
    }

    override fun vorbisSound(message: VorbisSound) {
        if (!filters[PropertyFilter.SYNTH_SOUND]) return omit()
        root.children += AnyProperty("sound", Rs3GamevalLookup.sound(message.soundId), String::class.java)
        root.children += AnyProperty("volume", message.volume, Int::class.java)
        root.children += AnyProperty("loops", message.loops, Int::class.java)
        root.children += AnyProperty("delay", message.delay, Int::class.java)
        root.children += AnyProperty("pitch", message.pitch, Int::class.java)
    }

    override fun runClientScript(message: RunClientScript) {
        if (!filters[PropertyFilter.RUNCLIENTSCRIPT]) return omit()
        root.children += AnyProperty("script", Rs3GamevalLookup.cs2(message.id), String::class.java)
        root.children += AnyProperty("types", String(message.types), String::class.java)
        root.children += AnyProperty("args", message.values.toString(), String::class.java)
    }

    override fun projAnimSpecificV2(message: ProjAnimSpecificV2) {
        if (!filters[PropertyFilter.PROJANIM_SPECIFIC]) return omit()
        root.children += AnyProperty("spotAnim", Rs3GamevalLookup.seq(message.spotAnimId), String::class.java)
        root.children += AnyProperty("field1", message.field1, Int::class.java)
        root.children += AnyProperty("field2", message.field2, Int::class.java)
        root.children += AnyProperty("field4", message.field4, Int::class.java)
        root.children += AnyProperty("field5", message.field5, Int::class.java)
        root.children += AnyProperty("field6", message.field6, Int::class.java)
        root.children += AnyProperty("field7", message.field7, Int::class.java)
        root.children += AnyProperty("field8", message.field8, Int::class.java)
        root.children += AnyProperty("field9", message.field9, Int::class.java)
        root.children += AnyProperty("field10", message.field10, Int::class.java)
        root.children += AnyProperty("field11", message.field11, Int::class.java)
        root.children += AnyProperty("field12", message.field12, Int::class.java)
        root.children += AnyProperty("field13", message.field13, Int::class.java)
        root.children += AnyProperty("field14", message.field14, Int::class.java)
        root.children += AnyProperty("field15", message.field15, Int::class.java)
        root.children += AnyProperty("field16", message.field16, Int::class.java)
        root.children += AnyProperty("field17", message.field17, Int::class.java)
    }

    override fun locPrefetch(message: LocPrefetch) {
        if (!filters[PropertyFilter.LOC_ADD_CHANGE]) return omit()
        root.children += AnyProperty("loc", Rs3GamevalLookup.loc(message.id), String::class.java)
        root.children += AnyProperty("shape", message.shape, Int::class.java)
        root.children += AnyProperty("rotation", message.rotation, Int::class.java)
    }

    override fun cutscene2dPlay(message: Cutscene2dPlay) {
        root.children += AnyProperty("id", message.id, Int::class.java)
    }

    override fun jcoinsUpdate(message: JcoinsUpdate) {
        root.children += AnyProperty("jcoins", message.jcoins, Int::class.java)
    }

    override fun unknownServerOpcode(message: UnknownServerPacket) {
        if (!filters[PropertyFilter.UNKNOWN_SERVER_OPCODE_HEX]) return omit()
        root.children += AnyProperty("opcode", message.opcode, Int::class.java)
        root.children += AnyProperty("name", message.name, String::class.java)
        root.children += AnyProperty("bytes", hex(message.bytes), String::class.java)
    }
}
