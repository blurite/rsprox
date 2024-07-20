package net.rsprox.protocol.game.outgoing.model.info.npcinfo.extendedinfo.customisation

public sealed interface CustomisationType

public data object ResetCustomisation : CustomisationType

public class ModelCustomisation(
    public val models: List<Int>?,
    public val recolours: List<Int>?,
    public val retextures: List<Int>?,
    public val mirror: Boolean?,
) : CustomisationType {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelCustomisation

        if (models != other.models) return false
        if (recolours != other.recolours) return false
        if (retextures != other.retextures) return false
        if (mirror != other.mirror) return false

        return true
    }

    override fun hashCode(): Int {
        var result = models?.hashCode() ?: 0
        result = 31 * result + (recolours?.hashCode() ?: 0)
        result = 31 * result + (retextures?.hashCode() ?: 0)
        result = 31 * result + (mirror?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ModelCustomisation(" +
            "models=$models, " +
            "recolours=$recolours, " +
            "retextures=$retextures, " +
            "mirror=$mirror" +
            ")"
    }
}
