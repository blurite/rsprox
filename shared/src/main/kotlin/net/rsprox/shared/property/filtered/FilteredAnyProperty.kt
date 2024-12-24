package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty

public class FilteredAnyProperty<T>(
    override val propertyName: String,
    override val value: T,
    override val type: Class<T>,
    override val filterValue: T,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
) : ChildProperty<T>,
    FilteredProperty<T>
