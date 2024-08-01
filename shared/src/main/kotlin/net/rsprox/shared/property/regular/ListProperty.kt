package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class ListProperty(
    override val propertyName: String,
    override val value: Unit = Unit,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Unit> = Unit::class.java,
) : ChildProperty<Unit>
