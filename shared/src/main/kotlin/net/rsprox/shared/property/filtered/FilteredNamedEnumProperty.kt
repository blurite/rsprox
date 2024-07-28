package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty
import net.rsprox.shared.property.NamedEnum

public class FilteredNamedEnumProperty<T>(
    override val propertyName: String,
    override val value: T,
    override val filterValue: T,
    override val type: Class<T>,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
) : FilteredProperty<T>,
    ChildProperty<T> where T : Enum<T>, T : NamedEnum
