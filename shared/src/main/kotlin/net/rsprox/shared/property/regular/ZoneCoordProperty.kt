package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

@Suppress("MemberVisibilityCanBePrivate")
public class ZoneCoordProperty(
    override val propertyName: String,
    public val level: Int,
    public val zoneX: Int,
    public val zoneZ: Int,
    override val value: Int = (level and 0x3 shl 22) or (zoneX and 0x7FF shl 11) or (zoneZ and 0x7FF),
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Int> = Int::class.java,
) : ChildProperty<Int>
