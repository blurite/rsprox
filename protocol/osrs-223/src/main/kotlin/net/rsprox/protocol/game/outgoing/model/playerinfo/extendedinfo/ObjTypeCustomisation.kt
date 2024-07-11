package net.rsprox.protocol.game.outgoing.model.playerinfo.extendedinfo

public class ObjTypeCustomisation(
    public val recolIndices: Int,
    public val recol1: Int,
    public val recol2: Int,
    public val retexIndices: Int,
    public val retex1: Int,
    public val retex2: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ObjTypeCustomisation

        if (recolIndices != other.recolIndices) return false
        if (recol1 != other.recol1) return false
        if (recol2 != other.recol2) return false
        if (retexIndices != other.retexIndices) return false
        if (retex1 != other.retex1) return false
        if (retex2 != other.retex2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = recolIndices
        result = 31 * result + recol1
        result = 31 * result + recol2
        result = 31 * result + retexIndices
        result = 31 * result + retex1
        result = 31 * result + retex2
        return result
    }

    override fun toString(): String {
        return "ObjTypeCustomisation(" +
            "recolIndices=$recolIndices, " +
            "recol1=$recol1, " +
            "recol2=$recol2, " +
            "retexIndices=$retexIndices, " +
            "retex1=$retex1, " +
            "retex2=$retex2" +
            ")"
    }
}
