package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty

public class FilteredIntProperty(
    override val propertyName: String,
    override val value: Int,
    override val filterValue: Int,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Int> = Int::class.java,
) : ChildProperty<Int>,
    FilteredProperty<Int>
