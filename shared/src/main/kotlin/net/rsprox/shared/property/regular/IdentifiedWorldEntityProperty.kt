package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class IdentifiedWorldEntityProperty(
    override val propertyName: String,
    public val index: Int,
    public val level: Int,
    public val x: Int,
    public val z: Int,
    sizeX: Int,
    sizeZ: Int,
    centerFineOffsetX: Int?,
    centerFineOffsetZ: Int?,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String> {
    override val value: String =
        if (centerFineOffsetX != null && centerFineOffsetZ != null) {
            "(index=$index, coord=($x, $z, $level), " +
                "sizex=$sizeX, sizez=$sizeZ, offsetx=$centerFineOffsetX, offsetz=$centerFineOffsetZ)"
        } else {
            "(index=$index, coord=($x, $z, $level), sizex=$sizeX, sizez=$sizeZ)"
        }
}
