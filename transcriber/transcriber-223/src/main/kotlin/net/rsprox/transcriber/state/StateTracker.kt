package net.rsprox.transcriber.state

public class StateTracker {
    public var cycle: Int = 0
        private set
    private var activeWorldId: Int = ROOT_WORLD
    private val worlds: MutableMap<Int, World> = mutableMapOf()

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

    public companion object {
        public const val ROOT_WORLD: Int = -1
    }
}
