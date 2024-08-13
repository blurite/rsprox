package net.rsprox.transcriber.base.impl

import net.rsprox.cache.api.Cache
import net.rsprox.protocol.common.CoordGrid
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
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.PropertyFilterSetStore
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.boolean
import net.rsprox.shared.property.coordGrid
import net.rsprox.shared.property.filteredBoolean
import net.rsprox.shared.property.filteredInt
import net.rsprox.shared.property.filteredScriptVarType
import net.rsprox.shared.property.filteredString
import net.rsprox.shared.property.group
import net.rsprox.shared.property.identifiedMultinpc
import net.rsprox.shared.property.identifiedNpc
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.namedEnum
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType
import net.rsprox.shared.property.shortPlayer
import net.rsprox.shared.property.string
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.transcriber.base.firstOfInstanceOfNull
import net.rsprox.transcriber.base.maxUShortToMinusOne
import net.rsprox.transcriber.impl.PlayerInfoTranscriber
import net.rsprox.transcriber.state.Player
import net.rsprox.transcriber.state.StateTracker

@Suppress("DuplicatedCode")
public class BasePlayerInfoTranscriber(
    private val stateTracker: StateTracker,
    private val monitor: SessionMonitor<*>,
    private val cache: Cache,
    private val filterSetStore: PropertyFilterSetStore,
) : PlayerInfoTranscriber {
    private val root: RootProperty<*>
        get() = checkNotNull(stateTracker.root.last())
    private val filters: PropertyFilterSet
        get() = filterSetStore.getActive()

    private fun omit() {
        stateTracker.deleteRoot()
    }

    private fun Property.entity(ambiguousIndex: Int): ChildProperty<*> {
        return if (ambiguousIndex >= 0x10000) {
            player(ambiguousIndex - 0x10000)
        } else {
            npc(ambiguousIndex)
        }
    }

    private fun Property.npc(index: Int): ChildProperty<*> {
        val world = stateTracker.getActiveWorld()
        val npc = world.getNpcOrNull(index) ?: return unidentifiedNpc(index)
        val finalIndex =
            if (filters[PropertyFilter.NPC_OMIT_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        val multinpc = stateTracker.resolveMultinpc(npc.id, cache)
        return if (multinpc != null) {
            identifiedMultinpc(
                finalIndex,
                npc.id,
                multinpc.id,
                multinpc.name,
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        } else {
            identifiedNpc(
                finalIndex,
                npc.id,
                npc.name ?: "null",
                npc.coord.level,
                npc.coord.x,
                npc.coord.z,
            )
        }
    }

    private fun Property.player(
        index: Int,
        name: String = "player",
    ): ChildProperty<*> {
        val player = stateTracker.getPlayerOrNull(index)
        val finalIndex =
            if (filters[PropertyFilter.PLAYER_OMIT_INDEX]) {
                Int.MIN_VALUE
            } else {
                index
            }
        return if (player != null) {
            identifiedPlayer(
                finalIndex,
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

    private fun Property.shortPlayer(
        index: Int,
        name: String = "player",
    ): ChildProperty<*> {
        val player = stateTracker.getPlayerOrNull(index)
        return if (player != null) {
            shortPlayer(
                Int.MIN_VALUE,
                player.name,
                name,
            )
        } else {
            shortPlayer(
                index,
                null,
                name,
            )
        }
    }

    private fun Property.coordGrid(
        name: String,
        coordGrid: CoordGrid,
    ): ScriptVarTypeProperty<*> {
        return coordGrid(coordGrid.level, coordGrid.x, coordGrid.z, name)
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

    override fun playerInfo(message: PlayerInfo) {
        stateTracker.clearTempMoveSpeeds()
        // Assign the coord and name of each player that is being added
        preloadPlayerInfo(message)
        // Log any activities that happened for all the players
        logPlayerInfo(message)
        // Update the last known coord and name of each player being processed
        postPlayerInfo(message)
    }

    private fun logPlayerInfo(message: PlayerInfo) {
        if (!filters[PropertyFilter.PLAYER_INFO]) return omit()
        val localPlayerOnly = filters[PropertyFilter.PLAYER_INFO_LOCAL_PLAYER_ONLY]
        val group =
            root.group {
                for ((index, update) in message.updates) {
                    if (localPlayerOnly && index != stateTracker.localPlayerIndex) continue
                    when (update) {
                        is PlayerUpdateType.LowResolutionMovement, PlayerUpdateType.LowResolutionIdle -> {
                            // no-op
                        }
                        is PlayerUpdateType.HighResolutionIdle -> {
                            if (update.extendedInfo.isEmpty()) {
                                return@group
                            }
                            val player = stateTracker.getPlayer(index)
                            group("IDLE") {
                                player(index)
                                appendExtendedInfo(player, update.extendedInfo)
                            }
                        }
                        is PlayerUpdateType.HighResolutionMovement -> {
                            if (filters[PropertyFilter.PLAYER_INFO_OMIT_NO_EXTENDED_INFO] &&
                                update.extendedInfo.isEmpty()
                            ) {
                                continue
                            }
                            val player = stateTracker.getPlayer(index)
                            val speed = getMoveSpeed(stateTracker.getMoveSpeed(index))
                            val label =
                                when (speed) {
                                    MoveSpeed.CRAWL -> "CRAWL"
                                    MoveSpeed.WALK -> "WALK"
                                    MoveSpeed.RUN -> "RUN"
                                    MoveSpeed.TELEPORT -> "TELEPORT"
                                    MoveSpeed.STATIONARY -> "STATIONARY"
                                }
                            group(label) {
                                player(index)
                                coordGrid("newcoord", update.coord)
                                appendExtendedInfo(player, update.extendedInfo)
                            }
                        }
                        is PlayerUpdateType.HighResolutionToLowResolution -> {
                            if (filters[PropertyFilter.PLAYER_REMOVAL]) {
                                group("DEL") {
                                    player(index)
                                }
                            }
                        }
                        is PlayerUpdateType.LowResolutionToHighResolution -> {
                            if (filters[PropertyFilter.PLAYER_INFO_OMIT_NO_EXTENDED_INFO] &&
                                update.extendedInfo.isEmpty()
                            ) {
                                continue
                            }
                            val player = stateTracker.getPlayer(index)
                            group("ADD") {
                                player(index)
                                appendExtendedInfo(player, update.extendedInfo)
                            }
                        }
                    }
                }
            }
        val children = group.children
        // If no children were added to the root group, it means no players are being updated
        // In this case, remove the empty line that the group is generating
        if (children.isEmpty()) {
            if (filters[PropertyFilter.PLAYER_INFO_OMIT_EMPTY]) {
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
        player: Player,
        extendedInfo: List<ExtendedInfo>,
    ) {
        for (info in extendedInfo) {
            when (info) {
                is ChatExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_CHAT]) {
                        group("CHAT") {
                            appendChatExtendedInfo(player, info)
                        }
                    }
                }
                is FaceAngleExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_FACE_ANGLE]) {
                        group("FACE_ANGLE") {
                            appendFaceAngleExtendedInfo(player, info)
                        }
                    }
                }
                is MoveSpeedExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_MOVE_SPEED]) {
                        group("MOVE_SPEED") {
                            appendMoveSpeedExtendedInfo(player, info)
                        }
                    }
                }
                is TemporaryMoveSpeedExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_MOVE_SPEED]) {
                        group("TEMP_MOVE_SPEED") {
                            appendTemporaryMoveSpeedExtendedInfo(player, info)
                        }
                    }
                }
                is NameExtrasExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_NAME_EXTRAS]) {
                        group("NAME_EXTRAS") {
                            appendNameExtrasExtendedInfo(player, info)
                        }
                    }
                }
                is SayExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_SAY]) {
                        group("SAY") {
                            appendSayExtendedInfo(player, info)
                        }
                    }
                }
                is SequenceExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_SEQUENCE]) {
                        group("SEQUENCE") {
                            appendSequenceExtendedInfo(player, info)
                        }
                    }
                }
                is ExactMoveExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_EXACTMOVE]) {
                        group("EXACTMOVE") {
                            appendExactMoveExtendedInfo(player, info)
                        }
                    }
                }
                is HitExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_HITS]) {
                        appendHitExtendedInfo(player, info)
                    }
                }
                is TintingExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_TINTING]) {
                        group("TINTING") {
                            appendTintingExtendedInfo(player, info)
                        }
                    }
                }
                is SpotanimExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_SPOTANIMS]) {
                        appendSpotanimExtendedInfo(player, info)
                    }
                }
                is FacePathingEntityExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_FACE_PATHINGENTITY]) {
                        group("FACE_PATHINGENTITY") {
                            appendFacePathingEntityExtendedInfo(player, info)
                        }
                    }
                }
                is AppearanceExtendedInfo -> {
                    if (filters[PropertyFilter.PLAYER_APPEARANCE]) {
                        group("APPEARANCE") {
                            appendAppearanceExtendedInfo(player, info)
                        }
                    }
                }
                else -> error("Unknown extended info: $info")
            }
        }
    }

    private fun Property.appendChatExtendedInfo(
        player: Player,
        info: ChatExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        string("text", info.text)
        filteredBoolean("autotyper", info.autotyper)
        filteredInt("colour", info.colour, 0)
        filteredInt("effects", info.effects, 0)
        filteredInt("chatcrown", info.modIcon, 0)
        filteredString("pattern", info.pattern?.contentToString(), null)
    }

    private fun Property.appendFaceAngleExtendedInfo(
        player: Player,
        info: FaceAngleExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        int("angle", info.angle)
    }

    private enum class MoveSpeed(
        override val prettyName: String,
    ) : NamedEnum {
        CRAWL("crawl"),
        WALK("walk"),
        RUN("run"),
        TELEPORT("teleport"),
        STATIONARY("stationary"),
    }

    private fun getMoveSpeed(id: Int): MoveSpeed {
        return when (id) {
            0 -> MoveSpeed.CRAWL
            1 -> MoveSpeed.WALK
            2 -> MoveSpeed.RUN
            127 -> MoveSpeed.TELEPORT
            -1, 255 -> MoveSpeed.STATIONARY // TODO: Figure out when 127 vs -1 gets used
            else -> error("Unknown move speed: $id")
        }
    }

    private fun Property.appendMoveSpeedExtendedInfo(
        player: Player,
        info: MoveSpeedExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        namedEnum("speed", getMoveSpeed(info.speed))
    }

    private fun Property.appendTemporaryMoveSpeedExtendedInfo(
        player: Player,
        info: TemporaryMoveSpeedExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        namedEnum("speed", getMoveSpeed(info.speed))
    }

    private fun Property.appendNameExtrasExtendedInfo(
        player: Player,
        info: NameExtrasExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        string("beforename", info.beforeName)
        string("aftername", info.afterName)
        string("afterlevel", info.afterCombatLevel)
    }

    private fun Property.appendSayExtendedInfo(
        player: Player,
        info: SayExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        string("text", info.text)
    }

    private fun Property.appendSequenceExtendedInfo(
        player: Player,
        info: SequenceExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        scriptVarType("id", ScriptVarType.SEQ, info.id.maxUShortToMinusOne())
        filteredInt("delay", info.delay, 0)
    }

    private fun Property.appendExactMoveExtendedInfo(
        player: Player,
        info: ExactMoveExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        val curX = player.coord.x
        val curZ = player.coord.z
        val level = player.coord.level
        coordGrid("to1", CoordGrid(level, curX - info.deltaX2, curZ - info.deltaZ2))
        int("delay1", info.delay1)
        coordGrid("to2", CoordGrid(level, curX - info.deltaX1, curZ - info.deltaZ1))
        int("delay2", info.delay2)
        int("angle", info.direction)
    }

    private fun Property.appendHitExtendedInfo(
        player: Player,
        info: HitExtendedInfo,
    ) {
        for (hit in info.hits) {
            group("HIT") {
                if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
                    shortPlayer(player.index)
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
                if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
                    shortPlayer(player.index)
                }
                scriptVarType("id", ScriptVarType.HEADBAR, headbar.type)
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

    private fun Property.appendTintingExtendedInfo(
        player: Player,
        info: TintingExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        int("start", info.start)
        int("end", info.end)
        int("hue", info.hue)
        int("saturation", info.saturation)
        int("lightness", info.lightness)
        int("weight", info.weight)
    }

    private fun Property.appendSpotanimExtendedInfo(
        player: Player,
        info: SpotanimExtendedInfo,
    ) {
        for ((slot, spotanim) in info.spotanims) {
            group("SPOTANIM") {
                if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
                    shortPlayer(player.index)
                }
                filteredInt("slot", slot, 0)
                scriptVarType("id", ScriptVarType.SPOTANIM, spotanim.id.maxUShortToMinusOne())
                filteredInt("delay", spotanim.delay, 0)
                filteredInt("height", spotanim.height, 0)
            }
        }
    }

    private fun Property.appendFacePathingEntityExtendedInfo(
        player: Player,
        info: FacePathingEntityExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        if (info.index == 0xFFFFFF) {
            string("entity", null)
        } else {
            entity(info.index)
        }
    }

    private enum class WearPos(
        val id: Int,
        override val prettyName: String,
    ) : NamedEnum {
        HAT(0, "hat"),
        BACK(1, "back"),
        FRONT(2, "front"),
        RIGHTHAND(3, "righthand"),
        TORSO(4, "torso"),
        LEFTHAND(5, "lefthand"),
        ARMS(6, "arms"),
        LEGS(7, "legs"),
        HEAD(8, "head"),
        HANDS(9, "hands"),
        FEET(10, "feet"),
        JAW(11, "jaw"),
        RING(12, "ring"),
        QUIVER(13, "quiver"),
    }

    private fun Property.appendAppearanceExtendedInfo(
        player: Player,
        info: AppearanceExtendedInfo,
    ) {
        if (filters[PropertyFilter.PLAYER_EXT_INFO_INLINE]) {
            shortPlayer(player.index)
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_DETAILS]) {
            group("DETAILS") {
                string("name", info.name)
                int("combatlevel", info.combatLevel)
                filteredInt("skilllevel", info.skillLevel, 0)
                int("gender", info.gender)
                int("textgender", info.textGender)
            }
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_STATUS]) {
            val statusGroup =
                group("STATUS") {
                    filteredBoolean("hidden", info.hidden)
                    filteredInt("skullicon", info.skullIcon, -1)
                    filteredInt("overheadicon", info.overheadIcon, -1)
                    filteredScriptVarType("npc", ScriptVarType.NPC, info.transformedNpcId, -1)
                }
            if (statusGroup.children.isEmpty()) {
                children.removeLast()
            }
        }
        if (info.transformedNpcId == -1) {
            if (filters[PropertyFilter.PLAYER_APPEARANCE_EQUIPMENT]) {
                group("EQUIPMENT") {
                    for ((index, value) in info.identKit.withIndex()) {
                        if (value >= 512) {
                            val pos = WearPos.entries.first { it.id == index }
                            group {
                                namedEnum("wearpos", pos)
                                scriptVarType("id", ScriptVarType.OBJ, value - 512)
                            }
                        }
                    }
                }
            }
            if (filters[PropertyFilter.PLAYER_APPEARANCE_IDENTKIT]) {
                group("IDENTKIT") {
                    for ((index, value) in info.identKit.withIndex()) {
                        if (value in 256..<512) {
                            val pos = WearPos.entries.first { it.id == index }
                            group {
                                namedEnum("wearpos", pos)
                                scriptVarType("id", ScriptVarType.IDKIT, value - 256)
                            }
                        }
                    }
                }
            }
            if (filters[PropertyFilter.PLAYER_APPEARANCE_IF_IDENTKIT]) {
                group("INTERFACE_IDENTKIT") {
                    for ((index, value) in info.interfaceIdentKit.withIndex()) {
                        if (value in 256..<512) {
                            val pos = WearPos.entries.first { it.id == index }
                            group {
                                namedEnum("wearpos", pos)
                                scriptVarType("id", ScriptVarType.IDKIT, value - 256)
                            }
                        }
                    }
                }
            }
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_COLOURS]) {
            group("COLOURS") {
                for ((index, value) in info.colours.withIndex()) {
                    int("colour$index", value)
                }
            }
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_BAS]) {
            group("BAS") {
                scriptVarType("ready", ScriptVarType.SEQ, info.readyAnim.maxUShortToMinusOne())
                scriptVarType("turn", ScriptVarType.SEQ, info.turnAnim.maxUShortToMinusOne())
                scriptVarType("walk", ScriptVarType.SEQ, info.walkAnim.maxUShortToMinusOne())
                scriptVarType("walkback", ScriptVarType.SEQ, info.walkAnimBack.maxUShortToMinusOne())
                scriptVarType("walkleft", ScriptVarType.SEQ, info.walkAnimLeft.maxUShortToMinusOne())
                scriptVarType("walkright", ScriptVarType.SEQ, info.walkAnimRight.maxUShortToMinusOne())
                scriptVarType("run", ScriptVarType.SEQ, info.runAnim.maxUShortToMinusOne())
            }
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_NAME_EXTRAS]) {
            group("NAME_EXTRAS") {
                string("beforename", info.beforeName)
                string("aftername", info.afterName)
                string("afterlevel", info.afterCombatLevel)
            }
        }
        if (filters[PropertyFilter.PLAYER_APPEARANCE_OBJ_TYPE_CUSTOMIZATION]) {
            val objTypeCustomisationGroup =
                group("OBJ_TYPE_CUSTOMISATION") {
                    boolean("forcemodelrefresh", info.forceModelRefresh)
                    val customisation = info.objTypeCustomisation
                    if (customisation != null) {
                        for ((index, cus) in customisation.withIndex()) {
                            if (cus == null) {
                                continue
                            }
                            val pos = WearPos.entries.first { it.id == index }
                            namedEnum("wearpos", pos)
                            val recolIndex1 = cus.recolIndices and 0xF
                            val recolIndex2 = cus.recolIndices ushr 4 and 0xF
                            if (recolIndex1 != 0xF) {
                                int("recol$recolIndex1", cus.recol1)
                            }
                            if (recolIndex2 != 0xF) {
                                int("recol$recolIndex2", cus.recol2)
                            }

                            val retexIndex1 = cus.retexIndices and 0xF
                            val retexIndex2 = cus.retexIndices ushr 4 and 0xF
                            if (retexIndex1 != 0xF) {
                                scriptVarType("retex$retexIndex1", ScriptVarType.TEXTURE, cus.retex1)
                            }
                            if (retexIndex2 != 0xF) {
                                scriptVarType("retex$retexIndex2", ScriptVarType.TEXTURE, cus.retex2)
                            }
                        }
                    }
                }
            if (objTypeCustomisationGroup.children.isEmpty()) {
                children.removeLast()
            }
        }
    }
}
