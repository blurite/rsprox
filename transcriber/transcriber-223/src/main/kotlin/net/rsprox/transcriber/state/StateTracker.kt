package net.rsprox.transcriber.state

import net.rsprot.protocol.Prot
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt

public class StateTracker {
    public var cycle: Int = 0
        private set
    private var activeWorldId: Int = ROOT_WORLD
    private val worlds: MutableMap<Int, World> = mutableMapOf()
    private val players: MutableMap<Int, Player> = mutableMapOf()
    private val lastKnownPlayerNames: MutableMap<Int, String> = mutableMapOf()
    public var currentProt: Prot = GameServerProt.REBUILD_NORMAL
    public var localPlayerIndex: Int = -1

    public fun createWorld(id: Int): World {
        val newWorld = World(id)
        val old = worlds.put(id, newWorld)
        check(old == null) {
            "Overriding existing world: $old"
        }
        return newWorld
    }

    public fun destroyWorld(id: Int) {
        worlds.remove(id)
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

    public companion object {
        public const val ROOT_WORLD: Int = -1
    }
}
