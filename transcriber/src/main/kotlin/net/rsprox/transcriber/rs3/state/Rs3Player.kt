package net.rsprox.transcriber.rs3.state

public data class Rs3Player(
    public val index: Int,
    public val name: String? = null,
    public val level: Int? = null,
    public val x: Int? = null,
    public val z: Int? = null,
) {
    public val hasCoord: Boolean
        get() = level != null && x != null && z != null
}
