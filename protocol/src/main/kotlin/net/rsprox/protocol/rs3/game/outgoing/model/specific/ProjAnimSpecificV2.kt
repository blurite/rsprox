package net.rsprox.protocol.rs3.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class ProjAnimSpecificV2(
    public val spotAnimId: Int,
    public val field1: Int,
    public val field2: Int,
    public val field4: Int,
    public val field5: Int,
    public val field6: Int,
    public val field7: Int,
    public val field8: Int,
    public val field9: Int,
    public val field10: Int,
    public val field11: Int,
    public val field12: Int,
    public val field13: Int,
    public val field14: Int,
    public val field15: Int,
    public val field16: Int,
    public val field17: Int,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjAnimSpecificV2

        if (spotAnimId != other.spotAnimId) return false
        if (field1 != other.field1) return false
        if (field2 != other.field2) return false
        if (field4 != other.field4) return false
        if (field5 != other.field5) return false
        if (field6 != other.field6) return false
        if (field7 != other.field7) return false
        if (field8 != other.field8) return false
        if (field9 != other.field9) return false
        if (field10 != other.field10) return false
        if (field11 != other.field11) return false
        if (field12 != other.field12) return false
        if (field13 != other.field13) return false
        if (field14 != other.field14) return false
        if (field15 != other.field15) return false
        if (field16 != other.field16) return false
        if (field17 != other.field17) return false

        return true
    }

    override fun hashCode(): Int {
        var result = spotAnimId
        result = 31 * result + field1
        result = 31 * result + field2
        result = 31 * result + field4
        result = 31 * result + field5
        result = 31 * result + field6
        result = 31 * result + field7
        result = 31 * result + field8
        result = 31 * result + field9
        result = 31 * result + field10
        result = 31 * result + field11
        result = 31 * result + field12
        result = 31 * result + field13
        result = 31 * result + field14
        result = 31 * result + field15
        result = 31 * result + field16
        result = 31 * result + field17
        return result
    }

    override fun toString(): String {
        return "ProjAnimSpecificV2(spotAnimId=$spotAnimId, field1=$field1, field2=$field2, " +
            "field4=$field4, field5=$field5, field6=$field6, field7=$field7, field8=$field8, " +
            "field9=$field9, field10=$field10, field11=$field11, field12=$field12, " +
            "field13=$field13, field14=$field14, field15=$field15, field16=$field16, field17=$field17)"
    }
}
