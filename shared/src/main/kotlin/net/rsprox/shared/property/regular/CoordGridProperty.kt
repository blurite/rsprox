package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

@Suppress("MemberVisibilityCanBePrivate")
public class CoordGridProperty(
    override val propertyName: String,
    public val level: Int,
    public val x: Int,
    public val z: Int,
    override val value: Int = (level and 0x3 shl 28) or (x and 0x3FFF shl 14) or (z and 0x3FFF),
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Int> = Int::class.java,
) : ChildProperty<Int>
