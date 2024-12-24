package net.rsprox.transcriber.text

import net.rsprox.cache.api.Cache
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.MoveSpeed
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcInfo
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.NpcUpdateType
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
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.ModelCustomisation
import net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation.ResetCustomisation
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExactMoveExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.ExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.FacePathingEntityExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.HitExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SayExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SequenceExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.SpotanimExtendedInfo
import net.rsprox.protocol.game.outgoing.model.info.shared.extendedinfo.TintingExtendedInfo
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.any
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.coordGridProperty
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.formattedInt
import net.rsprox.shared.property.group
import net.rsprox.shared.property.identifiedMultinpc
import net.rsprox.shared.property.identifiedNpc
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.shortNpc
import net.rsprox.shared.property.string
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet
import net.rsprox.shared.settings.SettingSetStore
import net.rsprox.transcriber.interfaces.NpcInfoTranscriber
import net.rsprox.transcriber.maxUShortToMinusOne
import net.rsprox.transcriber.state.Npc
import net.rsprox.transcriber.state.SessionState
import net.rsprox.transcriber.toFullBinaryString

@Suppress("SpellCheckingInspection", "DuplicatedCode")
public class TextNpcInfoTranscriber(
    private val sessionState: SessionState,
    private val cache: Cache,
    private val filterSetStore: PropertyFilterSetStore,
    private val settingSetStore: SettingSetStore,
) : NpcInfoTranscriber {
    private val root: RootProperty
        get() = checkNotNull(sessionState.root.last())
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()
    private val settings: SettingSet
        get() = settingSetStore.getActive()

    private fun omit() {
        sessionState.deleteRoot()
    }

    private fun Property.entity(ambiguousIndex: Int): ChildProperty<*> {
        return if (ambiguousIndex >= 0x10000) {
            player(ambiguousIndex - 0x10000)
        } else {
            npc(ambiguousIndex)
        }
    }

    private fun Property.shortNpc(
        index: Int,
        name: String = "npc",
    ): ChildProperty<*> {
        val npc = sessionState.getActiveWorld().getNpcOrNull(index)
        return shortNpc(
            index,
            npc?.id ?: -1,
            name,
        )
    }

    private fun Property.npc(index: Int): ChildProperty<*> {
        val world = sessionState.getActiveWorld()
        val npc = world.getNpcOrNull(index) ?: return unidentifiedNpc(index)
        val finalIndex =
            if (settings[Setting.HIDE_NPC_INDICES]) {
                Int.MIN_VALUE
            } else {
                index
            }
        val multinpc = sessionState.resolveMultinpc(npc.id, cache)
        val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(npc.coord)
        return if (multinpc != null) {
            identifiedMultinpc(
                finalIndex,
                npc.id,
                multinpc.id,
                multinpc.name,
                coord.level,
                coord.x,
                coord.z,
            )
        } else {
            identifiedNpc(
                finalIndex,
                npc.id,
                npc.name ?: "null",
                coord.level,
                coord.x,
                coord.z,
            )
        }
    }

    private fun Property.player(
        index: Int,
        name: String = "player",
    ): ChildProperty<*> {
        val player = sessionState.getPlayerOrNull(index)
        val finalIndex =
            if (settings[Setting.PLAYER_HIDE_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        return if (player != null) {
            val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(player.coord)
            identifiedPlayer(
                finalIndex,
                player.name,
                coord.level,
                coord.x,
                coord.z,
                name,
            )
        } else {
            unidentifiedPlayer(index, name)
        }
    }

    private fun Property.coordGrid(
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> {
        val coord = sessionState.getActiveWorld().getInstancedCoordOrSelf(CoordGrid(level, x, z))
        return coordGridProperty(coord.level, coord.x, coord.z, name)
    }

    override fun npcInfoV5(message: NpcInfo) {
        npcInfoUpdate(message)
    }

    private enum class NpcInfoStep(
        val id: Int,
        override val prettyName: String,
    ) : NamedEnum {
        NORTH_WEST(0, "north-west"),
        NORTH(1, "north"),
        NORTH_EAST(2, "north-east"),
        WEST(3, "west"),
        EAST(4, "east"),
        SOUTH_WEST(5, "south-west"),
        SOUTH(6, "south"),
        SOUTH_EAST(7, "south-east"),
    }

    private fun npcInfoUpdate(message: NpcInfo) {
        if (!filters[PropertyFilter.NPC_INFO]) return omit()
        val world = sessionState.getActiveWorld()
        val group =
            root.group {
                for ((index, update) in message.updates) {
                    when (update) {
                        is NpcUpdateType.Active -> {
                            if (settings[Setting.NPC_INFO_HIDE_INACTIVE_NPCS] &&
                                update.extendedInfo.isEmpty()
                            ) {
                                continue
                            }
                            val npc = world.getNpc(index)
                            val newCoord = CoordGrid(update.level, update.x, update.z)
                            val coordShift = npc.coord != newCoord
                            val movementType =
                                when (update.moveSpeed) {
                                    MoveSpeed.STATIONARY -> {
                                        when {
                                            coordShift && update.jump -> "TELEJUMP"
                                            coordShift -> "TELEPORT"
                                            else -> "idle"
                                        }
                                    }
                                    MoveSpeed.CRAWL -> "CRAWL"
                                    MoveSpeed.WALK -> "WALK"
                                    MoveSpeed.RUN -> "RUN"
                                }
                            group(movementType) {
                                npc(index)
                                if (update.steps.isNotEmpty()) {
                                    coordGrid(update.level, update.x, update.z, "newcoord")
                                    if (update.steps.size == 1) {
                                        val stepcode = update.steps.first()
                                        val step = NpcInfoStep.entries.first { it.id == stepcode }
                                        namedEnum("step", step)
                                    } else {
                                        check(update.steps.size == 2)
                                        val (stepcode1, stepcode2) = update.steps
                                        val step1 = NpcInfoStep.entries.first { it.id == stepcode1 }
                                        val step2 = NpcInfoStep.entries.first { it.id == stepcode2 }
                                        namedEnum("step1", step1)
                                        namedEnum("step2", step2)
                                    }
                                } else if (coordShift) {
                                    coordGrid(update.level, update.x, update.z, "newcoord")
                                }
                                appendExtendedInfo(npc, update.extendedInfo)
                            }
                        }
                        NpcUpdateType.HighResolutionToLowResolution -> {
                            if (settings[Setting.NPC_REMOVAL]) {
                                group("DEL") {
                                    npc(index)
                                }
                            }
                        }
                        is NpcUpdateType.LowResolutionToHighResolution -> {
                            if (settings[Setting.NPC_INFO_HIDE_INACTIVE_NPCS] &&
                                update.extendedInfo.isEmpty()
                            ) {
                                continue
                            }
                            val npc = world.getNpc(index)
                            group("ADD") {
                                npc(index)
                                filteredInt("creationcycle", update.spawnCycle, 0)
                                int("angle", update.angle)
                                filteredBoolean("jump", update.jump)
                                appendExtendedInfo(npc, update.extendedInfo)
                            }
                        }
                        NpcUpdateType.Idle -> {
                            // noop
                        }
                    }
                }
            }

        val children = group.children
        // If no children were added to the root group, it means no npcs are being updated
        // In this case, remove the empty line that the group is generating
        if (children.isEmpty()) {
            if (settings[Setting.NPC_INFO_HIDE_EMPTY]) {
                return omit()
            }
            root.children.clear()
            return
        }
        // Remove the empty line generated by the wrapper group
        root.children.clear()
        root.children.addAll(children)
    }

    private fun Property.appendExtendedInfo(
        npc: Npc,
        extendedInfo: List<ExtendedInfo>,
    ) {
        for (info in extendedInfo) {
            when (info) {
                is ExactMoveExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_EXACTMOVE]) {
                        group("EXACT_MOVE") {
                            exactMove(npc, info)
                        }
                    }
                }
                is FacePathingEntityExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_FACE_PATHINGENTITY]) {
                        group("FACE_PATHINGENTITY") {
                            facePathingEntity(npc, info)
                        }
                    }
                }
                is HitExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_HITS]) {
                        hits(npc, info)
                    }
                }
                is SayExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_SAY]) {
                        group("SAY") {
                            say(npc, info)
                        }
                    }
                }
                is SequenceExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_SEQUENCE]) {
                        group("SEQUENCE") {
                            sequence(npc, info)
                        }
                    }
                }
                is TintingExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_TINTING]) {
                        group("TINTING") {
                            tinting(npc, info)
                        }
                    }
                }
                is SpotanimExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_SPOTANIMS]) {
                        spotanim(npc, info)
                    }
                }
                is OldSpotanimExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_SPOTANIMS]) {
                        group("OLD_SPOTANIM") {
                            oldSpotanim(npc, info)
                        }
                    }
                }
                is BaseAnimationSetExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_BAS]) {
                        group("BAS") {
                            baseAnimationSet(npc, info)
                        }
                    }
                }
                is BodyCustomisationExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_BODY_CUSTOMISATION]) {
                        group("BODY_CUSTOMISATION") {
                            bodyCustomisation(npc, info)
                        }
                    }
                }
                is HeadCustomisationExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_HEAD_CUSTOMISATION]) {
                        group("HEAD_CUSTOMISATION") {
                            headCustomisation(npc, info)
                        }
                    }
                }
                is CombatLevelChangeExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_LEVEL_CHANGE]) {
                        group("LEVEL_CHANGE") {
                            combatLevelChange(npc, info)
                        }
                    }
                }
                is EnabledOpsExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_ENABLED_OPS]) {
                        group("ENABLED_OPS") {
                            enabledOps(npc, info)
                        }
                    }
                }
                is FaceCoordExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_FACE_COORD]) {
                        group("FACE_COORD") {
                            faceCoord(npc, info)
                        }
                    }
                }
                is NameChangeExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_NAME_CHANGE]) {
                        group("NAME_CHANGE") {
                            nameChange(npc, info)
                        }
                    }
                }
                is TransformationExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_TRANSFORMATION]) {
                        group("TRANSFORMATION") {
                            transformation(npc, info)
                        }
                    }
                }
                is HeadIconCustomisationExtendedInfo -> {
                    if (filters[PropertyFilter.NPC_HEADICON_CUSTOMISATION]) {
                        group("HEADICON_CUSTOMISATION") {
                            headIconCustomisation(npc, info)
                        }
                    }
                }
            }
        }
    }

    private fun Property.exactMove(
        npc: Npc,
        info: ExactMoveExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        val activeWorld = sessionState.getActiveWorld()
        val baseCoord = activeWorld.getInstancedCoordOrSelf(npc.coord)
        val to1 = CoordGrid(baseCoord.level, baseCoord.x - info.deltaX2, baseCoord.z - info.deltaZ2)
        coordGridProperty(to1.level, to1.x, to1.z, "to1")
        int("delay1", info.delay1)
        val to2 = CoordGrid(baseCoord.level, baseCoord.x - info.deltaX1, baseCoord.z - info.deltaZ1)
        coordGridProperty(to2.level, to2.x, to2.z, "to2")
        int("delay2", info.delay2)
        int("angle", info.direction)
    }

    private fun Property.facePathingEntity(
        npc: Npc,
        info: FacePathingEntityExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        if (info.index == 0xFFFFFF) {
            any<Any>("entity", null)
        } else {
            entity(info.index)
        }
    }

    private fun Property.hits(
        npc: Npc,
        info: HitExtendedInfo,
    ) {
        for (hit in info.hits) {
            group("HIT") {
                if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
                    shortNpc(npc.index)
                }
                scriptVarType("id", ScriptVarType.HITMARK, hit.type)
                int("value", hit.value)
                if (hit.soakType != -1) {
                    int("soaktype", hit.soakType)
                    int("soakvalue", hit.soakValue)
                }
                filteredInt("delay", hit.delay, 0)
            }
        }
        for (headbar in info.headbars) {
            group("HEADBAR") {
                if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
                    shortNpc(npc.index)
                }
                scriptVarType("id", ScriptVarType.HEADBAR, headbar.type)
                if (headbar.startFill == headbar.endFill) {
                    int("fill", headbar.startFill)
                } else {
                    int("startfill", headbar.startFill)
                    int("endfill", headbar.endFill)
                }
                if (headbar.startTime != 0 || headbar.endTime != 0) {
                    int("starttime", headbar.startTime)
                    int("endtime", headbar.endTime)
                }
            }
        }
    }

    private fun Property.say(
        npc: Npc,
        info: SayExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        string("text", info.text)
    }

    private fun Property.sequence(
        npc: Npc,
        info: SequenceExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        scriptVarType("id", ScriptVarType.SEQ, info.id.maxUShortToMinusOne())
        filteredInt("delay", info.delay, 0)
    }

    private fun Property.tinting(
        npc: Npc,
        info: TintingExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        int("start", info.start)
        int("end", info.end)
        int("hue", info.hue)
        int("saturation", info.saturation)
        int("lightness", info.lightness)
        int("weight", info.weight)
    }

    private fun Property.spotanim(
        npc: Npc,
        info: SpotanimExtendedInfo,
    ) {
        for ((slot, spotanim) in info.spotanims) {
            group("SPOTANIM") {
                if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
                    shortNpc(npc.index)
                }
                filteredInt("slot", slot, 0)
                scriptVarType("id", ScriptVarType.SPOTANIM, spotanim.id.maxUShortToMinusOne())
                filteredInt("delay", spotanim.delay, 0)
                filteredInt("height", spotanim.height, 0)
            }
        }
    }

    private fun Property.oldSpotanim(
        npc: Npc,
        info: OldSpotanimExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        scriptVarType("id", ScriptVarType.SPOTANIM, info.id.maxUShortToMinusOne())
        filteredInt("delay", info.delay, 0)
        filteredInt("height", info.height, 0)
    }

    private fun Property.baseAnimationSet(
        npc: Npc,
        info: BaseAnimationSetExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        val turnleft = info.turnLeftAnim
        val turnright = info.turnRightAnim
        val walk = info.walkAnim
        val walkback = info.walkAnimBack
        val walkleft = info.walkAnimLeft
        val walkright = info.walkAnimRight
        val run = info.runAnim
        val runback = info.runAnimBack
        val runleft = info.runAnimLeft
        val runright = info.runAnimRight
        val crawl = info.crawlAnim
        val crawlback = info.crawlAnimBack
        val crawlleft = info.crawlAnimLeft
        val crawlright = info.crawlAnimRight
        val ready = info.readyAnim
        if (turnleft != null) {
            scriptVarType("turnleft", ScriptVarType.SEQ, turnleft)
        }
        if (turnright != null) {
            scriptVarType("turnright", ScriptVarType.SEQ, turnright)
        }
        if (walk != null) {
            scriptVarType("walk", ScriptVarType.SEQ, walk)
        }
        if (walkback != null) {
            scriptVarType("walkback", ScriptVarType.SEQ, walkback)
        }
        if (walkleft != null) {
            scriptVarType("walkleft", ScriptVarType.SEQ, walkleft)
        }
        if (walkright != null) {
            scriptVarType("walkright", ScriptVarType.SEQ, walkright)
        }
        if (run != null) {
            scriptVarType("run", ScriptVarType.SEQ, run)
        }
        if (runback != null) {
            scriptVarType("runback", ScriptVarType.SEQ, runback)
        }
        if (runleft != null) {
            scriptVarType("runleft", ScriptVarType.SEQ, runleft)
        }
        if (runright != null) {
            scriptVarType("runright", ScriptVarType.SEQ, runright)
        }
        if (crawl != null) {
            scriptVarType("crawl", ScriptVarType.SEQ, crawl)
        }
        if (crawlback != null) {
            scriptVarType("crawlback", ScriptVarType.SEQ, crawlback)
        }
        if (crawlleft != null) {
            scriptVarType("crawlleft", ScriptVarType.SEQ, crawlleft)
        }
        if (crawlright != null) {
            scriptVarType("crawlright", ScriptVarType.SEQ, crawlright)
        }
        if (ready != null) {
            scriptVarType("ready", ScriptVarType.SEQ, ready)
        }
    }

    private fun Property.bodyCustomisation(
        npc: Npc,
        info: BodyCustomisationExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        when (val type = info.type) {
            is ModelCustomisation -> {
                val models = type.models
                if (models != null) {
                    for (model in models) {
                        group("MODEL") {
                            scriptVarType("id", ScriptVarType.MODEL, model)
                        }
                    }
                }
                val recol = type.recolours
                if (recol != null) {
                    group("RECOLOUR") {
                        for ((index, col) in recol.withIndex()) {
                            int("recol${index.inc()}d", col)
                        }
                    }
                }
                val retex = type.retextures
                if (retex != null) {
                    group("RECOLOUR") {
                        for ((index, col) in retex.withIndex()) {
                            scriptVarType("retex${index.inc()}d", ScriptVarType.TEXTURE, col)
                        }
                    }
                }
                val mirror = type.mirror
                if (mirror != null) {
                    boolean("mirror", mirror)
                }
            }
            ResetCustomisation -> {
            }
        }
    }

    private fun Property.headCustomisation(
        npc: Npc,
        info: HeadCustomisationExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        when (val type = info.type) {
            is ModelCustomisation -> {
                val models = type.models
                if (models != null) {
                    for (model in models) {
                        group("MODEL") {
                            scriptVarType("id", ScriptVarType.MODEL, model)
                        }
                    }
                }
                val recol = type.recolours
                if (recol != null) {
                    group("RECOLOUR") {
                        for ((index, col) in recol.withIndex()) {
                            int("recol${index.inc()}d", col)
                        }
                    }
                }
                val retex = type.retextures
                if (retex != null) {
                    group("RECOLOUR") {
                        for ((index, col) in retex.withIndex()) {
                            scriptVarType("retex${index.inc()}d", ScriptVarType.TEXTURE, col)
                        }
                    }
                }
                val mirror = type.mirror
                if (mirror != null) {
                    boolean("mirror", mirror)
                }
            }
            ResetCustomisation -> {
            }
        }
    }

    private fun Property.combatLevelChange(
        npc: Npc,
        info: CombatLevelChangeExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        formattedInt("level", info.level)
    }

    private fun Property.enabledOps(
        npc: Npc,
        info: EnabledOpsExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        any("opflags", info.value.toFullBinaryString(5))
    }

    private fun Property.faceCoord(
        npc: Npc,
        info: FaceCoordExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        var x = info.x
        if (x == 65535) {
            x = 16383
        }
        var z = info.z
        if (z == 65535) {
            z = 16383
        }
        coordGrid(npc.coord.level, x, z)
        filteredBoolean("instant", info.instant)
    }

    private fun Property.nameChange(
        npc: Npc,
        info: NameChangeExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        string("name", info.name)
    }

    private fun Property.transformation(
        npc: Npc,
        info: TransformationExtendedInfo,
    ) {
        if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
            shortNpc(npc.index)
        }
        scriptVarType("id", ScriptVarType.NPC, info.id)
    }

    private fun Property.headIconCustomisation(
        npc: Npc,
        info: HeadIconCustomisationExtendedInfo,
    ) {
        for (i in info.groups.indices) {
            val group = info.groups[i]
            val index = info.indices[i]
            if (group == -1 && index == -1) {
                continue
            }
            group("HEADICON") {
                if (settings[Setting.NPC_EXT_INFO_INDICATOR]) {
                    shortNpc(npc.index)
                }
                scriptVarType("id", ScriptVarType.GRAPHIC, group)
                int("index", index)
            }
        }
    }
}
