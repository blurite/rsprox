package net.rsprox.transcriber.base.impl

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
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.coordGrid
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.formattedInt
import net.rsprox.shared.property.group
import net.rsprox.shared.property.identifiedNpc
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.string
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.transcriber.base.maxUShortToMinusOne
import net.rsprox.transcriber.base.toFullBinaryString
import net.rsprox.transcriber.impl.NpcInfoTranscriber
import net.rsprox.transcriber.state.Npc
import net.rsprox.transcriber.state.StateTracker

@Suppress("SpellCheckingInspection", "DuplicatedCode")
public class BaseNpcInfoTranscriber(
    private val stateTracker: StateTracker,
    private val cache: Cache,
) : NpcInfoTranscriber {
    private val root: RootProperty<*>
        get() = stateTracker.root

    private fun Property.entity(ambiguousIndex: Int): ChildProperty<*> {
        return if (ambiguousIndex > 0xFFFF) {
            player(ambiguousIndex - 0xFFFF)
        } else {
            npc(ambiguousIndex)
        }
    }

    private fun Property.npc(index: Int): ChildProperty<*> {
        val npc = stateTracker.getActiveWorld().getNpcOrNull(index)
        return if (npc != null) {
            identifiedNpc(
                index,
                npc.id,
                npc.name ?: "null",
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        } else {
            unidentifiedNpc(index)
        }
    }

    private fun Property.player(
        index: Int,
        name: String = "player",
    ): ChildProperty<*> {
        val player = stateTracker.getPlayerOrNull(index)
        return if (player != null) {
            identifiedPlayer(
                index,
                player.name,
                player.coord.level,
                player.coord.x,
                player.coord.z,
                name,
            )
        } else {
            unidentifiedPlayer(index, name)
        }
    }

    private fun Property.coordGrid(
        name: String,
        coordGrid: CoordGrid,
    ): ScriptVarTypeProperty<*> {
        return coordGrid(coordGrid.level, coordGrid.x, coordGrid.z, name)
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

    override fun npcInfo(message: NpcInfo) {
        prenpcinfo(message)
        npcInfoUpdate(message)
        postnpcinfo(message)
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
        val world = stateTracker.getActiveWorld()
        val group =
            root.group {
                for ((index, update) in message.updates) {
                    when (update) {
                        is NpcUpdateType.Active -> {
                            val movementType =
                                when (update.moveSpeed) {
                                    MoveSpeed.STATIONARY -> "ACTIVE"
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
                                }
                                val npc = world.getNpc(index)
                                appendExtendedInfo(npc, update.extendedInfo)
                            }
                        }
                        NpcUpdateType.HighResolutionToLowResolution -> {
                            group("DEL") {
                                npc(index)
                            }
                        }
                        is NpcUpdateType.LowResolutionToHighResolution -> {
                            group("ADD") {
                                npc(index)
                                filteredInt("creationcycle", update.spawnCycle, 0)
                                int("angle", update.angle)
                                filteredBoolean("jump", update.jump)
                                val npc = world.getNpc(index)
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
                    group("EXACT_MOVE") {
                        exactMove(npc, info)
                    }
                }
                is FacePathingEntityExtendedInfo -> {
                    group("FACE_PATHINGENTITY") {
                        facePathingEntity(info)
                    }
                }
                is HitExtendedInfo -> {
                    hits(info)
                }
                is SayExtendedInfo -> {
                    group("SAY") {
                        say(info)
                    }
                }
                is SequenceExtendedInfo -> {
                    group("SEQUENCE") {
                        sequence(info)
                    }
                }
                is TintingExtendedInfo -> {
                    group("TINTING") {
                        tinting(info)
                    }
                }
                is SpotanimExtendedInfo -> {
                    spotanim(info)
                }
                is OldSpotanimExtendedInfo -> {
                    group("OLD_SPOTANIM") {
                        oldSpotanim(info)
                    }
                }
                is BaseAnimationSetExtendedInfo -> {
                    group("BAS") {
                        baseAnimationSet(info)
                    }
                }
                is BodyCustomisationExtendedInfo -> {
                    group("BODY_CUSTOMISATION") {
                        bodyCustomisation(info)
                    }
                }
                is HeadCustomisationExtendedInfo -> {
                    group("HEAD_CUSTOMISATION") {
                        headCustomisation(info)
                    }
                }
                is CombatLevelChangeExtendedInfo -> {
                    group("LEVEL_CHANGE") {
                        combatLevelChange(info)
                    }
                }
                is EnabledOpsExtendedInfo -> {
                    group("ENABLED_OPS") {
                        enabledOps(info)
                    }
                }
                is FaceCoordExtendedInfo -> {
                    group("FACE_COORD") {
                        faceCoord(npc, info)
                    }
                }
                is NameChangeExtendedInfo -> {
                    group("NAME_CHANGE") {
                        nameChange(info)
                    }
                }
                is TransformationExtendedInfo -> {
                    group("TRANSFORMATION") {
                        transformation(info)
                    }
                }
                is HeadIconCustomisationExtendedInfo -> {
                    group("HEADICON_CUSTOMISATION") {
                        headIconCustomisation(info)
                    }
                }
            }
        }
    }

    private fun Property.exactMove(
        npc: Npc,
        info: ExactMoveExtendedInfo,
    ) {
        val curX = npc.coord.x
        val curZ = npc.coord.z
        val level = npc.coord.level
        coordGrid("to1", CoordGrid(level, curX - info.deltaX2, curZ - info.deltaZ2))
        int("delay1", info.delay1)
        coordGrid("to2", CoordGrid(level, curX - info.deltaX1, curZ - info.deltaZ1))
        int("delay2", info.delay2)
        int("angle", info.direction)
    }

    private fun Property.facePathingEntity(info: FacePathingEntityExtendedInfo) {
        if (info.index == 0xFFFFFF) {
            string("entity", null)
        } else {
            entity(info.index)
        }
    }

    private fun Property.hits(info: HitExtendedInfo) {
        for (hit in info.hits) {
            group("HIT") {
                int("type", hit.type)
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
                int("type", headbar.type)
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

    private fun Property.say(info: SayExtendedInfo) {
        string("text", info.text)
    }

    private fun Property.sequence(info: SequenceExtendedInfo) {
        scriptVarType("id", ScriptVarType.SEQ, info.id.maxUShortToMinusOne())
        filteredInt("delay", info.delay, 0)
    }

    private fun Property.tinting(info: TintingExtendedInfo) {
        int("start", info.start)
        int("end", info.end)
        int("hue", info.hue)
        int("saturation", info.saturation)
        int("lightness", info.lightness)
        int("weight", info.weight)
    }

    private fun Property.spotanim(info: SpotanimExtendedInfo) {
        for ((slot, spotanim) in info.spotanims) {
            group("SPOTANIM") {
                filteredInt("slot", slot, 0)
                scriptVarType("id", ScriptVarType.SPOTANIM, spotanim.id)
                filteredInt("delay", spotanim.delay, 0)
                filteredInt("height", spotanim.height, 0)
            }
        }
    }

    private fun Property.oldSpotanim(info: OldSpotanimExtendedInfo) {
        scriptVarType("id", ScriptVarType.SPOTANIM, info.id)
        filteredInt("delay", info.delay, 0)
        filteredInt("height", info.height, 0)
    }

    private fun Property.baseAnimationSet(info: BaseAnimationSetExtendedInfo) {
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

    private fun Property.bodyCustomisation(info: BodyCustomisationExtendedInfo) {
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

    private fun Property.headCustomisation(info: HeadCustomisationExtendedInfo) {
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

    private fun Property.combatLevelChange(info: CombatLevelChangeExtendedInfo) {
        formattedInt("level", info.level)
    }

    private fun Property.enabledOps(info: EnabledOpsExtendedInfo) {
        string("opflags", info.value.toFullBinaryString(5))
    }

    private fun Property.faceCoord(
        npc: Npc,
        info: FaceCoordExtendedInfo,
    ) {
        coordGrid(npc.coord.level, info.x, info.z)
        filteredBoolean("instant", info.instant)
    }

    private fun Property.nameChange(info: NameChangeExtendedInfo) {
        string("name", info.name)
    }

    private fun Property.transformation(info: TransformationExtendedInfo) {
        scriptVarType("id", ScriptVarType.NPC, info.id)
    }

    private fun Property.headIconCustomisation(info: HeadIconCustomisationExtendedInfo) {
        for (i in info.groups.indices) {
            val group = info.groups[i]
            val index = info.indices[i]
            if (group == -1 && index == -1) {
                continue
            }
            group("HEADICON") {
                scriptVarType("id", ScriptVarType.GRAPHIC, group)
                int("index", index)
            }
        }
    }
}
