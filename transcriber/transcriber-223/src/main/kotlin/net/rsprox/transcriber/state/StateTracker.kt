package net.rsprox.transcriber.state

import net.rsprot.protocol.util.CombinedId
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.settings.SettingSetStore

public class StateTracker(
    private val settingSetStore: SettingSetStore,
) {
    public var cycle: Int = 0
        private set
    private var activeWorldId: Int = ROOT_WORLD
    private val worlds: MutableMap<Int, World> = mutableMapOf()
    private val players: MutableMap<Int, Player> = mutableMapOf()
    private val lastKnownPlayerNames: MutableMap<Int, String> = mutableMapOf()
    public var currentProt: String = GameServerProt.REBUILD_NORMAL.name
    public var localPlayerIndex: Int = -1
    private val openInterfaces: MutableMap<CombinedId, Int> = mutableMapOf()
    public var toplevelInterface: Int = -1
    private val cachedVarps: IntArray = IntArray(15_000)
    private lateinit var varpToVarbitsMap: Map<Int, List<VarBitType>>
    public var root: MutableList<RootProperty> = mutableListOf()
    private val cachedMoveSpeeds: IntArray =
        IntArray(2048) {
            -1
        }
    private val tempMoveSpeeds: MutableMap<Int, Int> = HashMap()
    private val experience: MutableMap<Int, Int> = HashMap()

    public fun setRoot() {
        this.root +=
            object : RootProperty {
                override val prot: String = currentProt
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }
    }

    public fun deleteRoot() {
        this.root.clear()
    }

    public fun getAllWorlds(): Collection<World> {
        return worlds.values
    }

    public fun createFakeServerRoot(name: String): RootProperty {
        val property =
            object : RootProperty {
                override val prot: String = name
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }
        this.root += property
        return property
    }

    public fun createWorld(id: Int): World {
        val newWorld = World(id, settingSetStore)
        val old = worlds.put(id, newWorld)
        check(old == null) {
            "Overriding existing world: $old"
        }
        return newWorld
    }

    public fun destroyDynamicWorlds() {
        worlds.keys.removeAll { it != -1 }
    }

    public fun destroyWorld(id: Int) {
        worlds.remove(id)
    }

    public fun getWorld(id: Int): World {
        return worlds[id] ?: error("World $id not available.")
    }

    public fun getWorldOrNull(id: Int): World? {
        return worlds[id]
    }

    public fun getActiveWorld(): World {
        return worlds[activeWorldId] ?: error("World $activeWorldId not available.")
    }

    public fun incrementCycle() {
        this.cycle++
    }

    public fun overridePlayer(player: Player) {
        players[player.index] = player
        lastKnownPlayerNames[player.index] = player.name
    }

    public fun removePlayer(player: Player) {
        removePlayer(player.index)
    }

    public fun removePlayer(index: Int) {
        players.remove(index)
    }

    public fun getPlayer(index: Int): Player {
        return players.getValue(index)
    }

    public fun getPlayerOrNull(index: Int): Player? {
        return players[index]
    }

    public fun getLastKnownPlayerName(index: Int): String? {
        return lastKnownPlayerNames[index]
    }

    public fun level(): Int {
        return getPlayer(localPlayerIndex).coord.level
    }

    public fun activeLevel(): Int {
        return getActiveWorld().activeLevel()
    }

    public fun openInterface(
        id: Int,
        com: CombinedId,
    ) {
        this.openInterfaces[com] = id
    }

    public fun moveInterface(
        sourceCom: CombinedId,
        destCom: CombinedId,
    ) {
        val opened = this.openInterfaces.remove(sourceCom) ?: return
        this.openInterfaces[destCom] = opened
    }

    public fun closeInterface(com: CombinedId) {
        this.openInterfaces.remove(com)
    }

    public fun getOpenInterface(com: CombinedId): Int? {
        return this.openInterfaces[com]
    }

    public fun getVarp(id: Int): Int {
        return cachedVarps[id]
    }

    public fun setVarp(
        id: Int,
        value: Int,
    ) {
        cachedVarps[id] = value
    }

    public fun getVarBit(varBit: VarBitType): Int {
        val varpValue = getVarp(varBit.basevar)
        return varBit.extract(varpValue)
    }

    public fun varbitsLoaded(): Boolean {
        return this::varpToVarbitsMap.isInitialized
    }

    public fun associateVarbits(list: Collection<VarBitType>) {
        val map = mutableMapOf<Int, MutableList<VarBitType>>()
        for (type in list) {
            map.getOrPut(type.basevar) { mutableListOf() }.add(type)
        }
        this.varpToVarbitsMap = map
    }

    public fun getAssociatedVarbits(basevar: Int): List<VarBitType> {
        return this.varpToVarbitsMap[basevar] ?: emptyList()
    }

    public fun setCachedMoveSpeed(
        index: Int,
        speed: Int,
    ) {
        this.cachedMoveSpeeds[index] = speed
    }

    public fun setTempMoveSpeed(
        index: Int,
        speed: Int,
    ) {
        this.tempMoveSpeeds[index] = speed
    }

    public fun clearTempMoveSpeeds() {
        this.tempMoveSpeeds.clear()
    }

    public fun getMoveSpeed(index: Int): Int {
        return tempMoveSpeeds[index] ?: cachedMoveSpeeds[index]
    }

    public fun getExperience(skill: Int): Int? {
        return experience[skill]
    }

    public fun setExperience(
        skill: Int,
        experience: Int,
    ) {
        this.experience[skill] = experience
    }

    public fun resolveMultinpc(
        baseId: Int,
        cache: Cache,
    ): NpcType? = cache.getNpcType(baseId)?.resolveMultinpc(cache)

    private fun NpcType.resolveMultinpc(cache: Cache): NpcType? =
        when {
            multivar in 1..<65535 -> {
                val state = getVarp(multivar)
                val multi = multinpc.getOrNull(state) ?: multinpc.last()
                cache.getNpcType(multi)
            }

            multivarbit in 1..<65535 -> {
                val varBit = cache.getVarBitType(multivarbit)
                if (varBit == null) {
                    null
                } else {
                    val state = getVarBit(varBit)
                    val multi = multinpc.getOrNull(state) ?: multinpc.last()
                    cache.getNpcType(multi)
                }
            }

            else -> null
        }

    public companion object {
        public const val ROOT_WORLD: Int = -1
    }
}
