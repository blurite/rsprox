package net.rsprox.protocol.game.outgoing.model.info.playerinfo.extendedinfo

public class ObjTypeCustomisation(
    public val recolIndices: Int,
    public val recol1: Int,
    public val recol2: Int,
    public val retexIndices: Int,
    public val retex1: Int,
    public val retex2: Int,
    public val manWear: Int,
    public val womanWear: Int,
    public val manHead: Int,
    public val womanHead: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ObjTypeCustomisation) return false

        if (recolIndices != other.recolIndices) return false
        if (recol1 != other.recol1) return false
        if (recol2 != other.recol2) return false
        if (retexIndices != other.retexIndices) return false
        if (retex1 != other.retex1) return false
        if (retex2 != other.retex2) return false
        if (manWear != other.manWear) return false
        if (womanWear != other.womanWear) return false
        if (manHead != other.manHead) return false
        if (womanHead != other.womanHead) return false

        return true
    }

    override fun hashCode(): Int {
        var result = recolIndices
        result = 31 * result + recol1
        result = 31 * result + recol2
        result = 31 * result + retexIndices
        result = 31 * result + retex1
        result = 31 * result + retex2
        result = 31 * result + manWear
        result = 31 * result + womanWear
        result = 31 * result + manHead
        result = 31 * result + womanHead
        return result
    }

    override fun toString(): String {
        return "ObjTypeCustomisation(" +
            "recolIndices=$recolIndices, " +
            "recol1=$recol1, " +
            "recol2=$recol2, " +
            "retexIndices=$retexIndices, " +
            "retex1=$retex1, " +
            "retex2=$retex2, " +
            "manWear=$manWear, " +
            "womanWear=$womanWear, " +
            "manHead=$manHead, " +
            "womanHead=$womanHead" +
            ")"
    }
}
