package net.rsprox.proxy.worlds

public data class World(
    public val id: Int,
    public val properties: Int,
    public val population: Int,
    public val location: Int,
    public val host: String,
    public val activity: String,
) {
    public val localHostAddress: LocalHostAddress = LocalHostAddress.fromWorldId(id)

    public fun hasFlag(flag: WorldFlag): Boolean {
        return properties and flag.bitflag != 0
    }

    override fun toString(): String {
        return "World(" +
            "id=$id, " +
            "properties=$properties, " +
            "population=$population, " +
            "location=$location, " +
            "host='$host', " +
            "activity='$activity', " +
            "localHostAddress=$localHostAddress" +
            ")"
    }
}
