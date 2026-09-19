package net.rsprox.protocol.rs3.game.outgoing.model.inv.util

public class InvVar(
    public val varId: Int,
    public val value: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvVar

        if (varId != other.varId) return false
        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        var result = varId
        result = 31 * result + value
        return result
    }

    override fun toString(): String {
        return "InvVar(varId=$varId, value=$value)"
    }
}
