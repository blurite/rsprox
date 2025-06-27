package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

@Suppress("MemberVisibilityCanBePrivate")
public class DecimalCoordGridProperty(
    override val propertyName: String,
    public val decimalCoordGrid: DecimalCoordGrid,
    override val value: DecimalCoordGrid = decimalCoordGrid,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<DecimalCoordGrid> = DecimalCoordGrid::class.java,
) : ChildProperty<DecimalCoordGrid>
