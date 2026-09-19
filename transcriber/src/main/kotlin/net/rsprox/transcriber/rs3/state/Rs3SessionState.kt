package net.rsprox.transcriber.rs3.state

import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty

public class Rs3SessionState(
    public val clientScripts: ClientScriptDefinitionProvider = ClientScriptDefinitionProvider.EMPTY,
    public val isLobby: Boolean = false,
) {
    public var cycle: Int = 0
        private set

    public var localPlayerIndex: Int = -1
    public var toplevelInterface: Int = -1
    private val openInterfaces: MutableMap<Long, Int> = mutableMapOf()

    private var activeWorldId: Int = ROOT_WORLD_ID

    private val worlds: MutableMap<Int, Rs3World> = mutableMapOf()
    private val players: MutableMap<Int, Rs3Player> = mutableMapOf()

    private val experience = LongArray(MAX_SKILLS) { -1L }

    public var currentProt: String = "UNKNOWN"

    public var root: MutableList<RootProperty> = mutableListOf()

    public fun incrementCycle() {
        this.cycle++
    }

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

    public fun createFakeServerRoot(name: String): RootProperty {
        val property =
            object : RootProperty {
                override val prot: String = name
                override val children: MutableList<ChildProperty<*>> = mutableListOf()
            }
        this.root += property
        return property
    }

    public fun openInterface(
        id: Int,
        com: Long,
    ) {
        this.openInterfaces[com] = id
    }

    public fun closeInterface(com: Long) {
        this.openInterfaces.remove(com)
    }

    public fun moveInterface(
        sourceCom: Long,
        destCom: Long,
    ) {
        val opened = this.openInterfaces.remove(sourceCom) ?: return
        this.openInterfaces[destCom] = opened
    }

    public fun getOpenInterface(com: Long): Int? {
        return this.openInterfaces[com]
    }

    public fun getActiveWorld(): Rs3World {
        return worlds.getOrPut(activeWorldId) { Rs3World() }
    }

    public fun setActiveWorld(id: Int) {
        this.activeWorldId = id
    }

    public fun overridePlayer(player: Rs3Player) {
        this.players[player.index] = player
    }

    public fun clearPlayers() {
        this.players.clear()
    }

    public fun removePlayer(index: Int) {
        this.players.remove(index)
    }

    public fun getPlayer(index: Int): Rs3Player {
        return checkNotNull(this.players[index]) { "No player tracked at index $index" }
    }

    public fun getPlayerOrNull(index: Int): Rs3Player? {
        return this.players[index]
    }

    public fun level(): Int {
        return checkNotNull(getPlayer(localPlayerIndex).level) {
            "No level tracked for local player at index $localPlayerIndex"
        }
    }

    public fun getExperience(skillId: Int): Long? {
        if (skillId !in 0 until MAX_SKILLS) return null
        val xp = experience[skillId]
        return if (xp == -1L) null else xp
    }

    public fun setExperience(
        skillId: Int,
        xp: Long,
    ) {
        if (skillId !in 0 until MAX_SKILLS) return
        experience[skillId] = xp
    }

    private companion object {
        const val ROOT_WORLD_ID = 0
        const val MAX_SKILLS = 29
    }
}
