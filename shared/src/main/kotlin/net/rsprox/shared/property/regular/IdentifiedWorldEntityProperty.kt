package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class IdentifiedWorldEntityProperty(
    override val propertyName: String,
    public val index: Int,
    public val level: Int,
    public val x: Int,
    public val z: Int,
    public val sizeX: Int,
    public val sizeZ: Int,
    override val value: String = "(index=$index, coord=($x, $z, $level), sizex=$sizeX, sizez=$sizeZ)",
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String>
