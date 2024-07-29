package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty

public class FilteredLongProperty(
    override val propertyName: String,
    override val value: Long,
    override val filterValue: Long,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Long> = Long::class.java,
) : ChildProperty<Long>,
    FilteredProperty<Long>
