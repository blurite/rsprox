package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class Hit(
    public val type: Int,
    public val value: Int,
    public val soakType: Int,
    public val soakValue: Int,
    public val delay: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Hit

        if (type != other.type) return false
        if (value != other.value) return false
        if (soakType != other.soakType) return false
        if (soakValue != other.soakValue) return false
        if (delay != other.delay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + value
        result = 31 * result + soakType
        result = 31 * result + soakValue
        result = 31 * result + delay
        return result
    }

    override fun toString(): String {
        return "Hit(" +
            "type=$type, " +
            "value=$value, " +
            "soakType=$soakType, " +
            "soakValue=$soakValue, " +
            "delay=$delay" +
            ")"
    }
}
