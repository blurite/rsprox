package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class Spotanim(
    public val id: Int,
    public val delay: Int,
    public val height: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Spotanim

        if (id != other.id) return false
        if (delay != other.delay) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + delay
        result = 31 * result + height
        return result
    }

    override fun toString(): String {
        return "Spotanim(" +
            "id=$id, " +
            "delay=$delay, " +
            "height=$height" +
            ")"
    }
}
