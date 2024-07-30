package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty
import net.rsprox.shared.property.NamedEnum
import net.rsprox.shared.property.regular.NamedEnumProperty

public class FilteredNamedEnumProperty<T>(
    propertyName: String,
    value: T,
    override val filterValue: T,
    type: Class<T>,
    children: MutableList<ChildProperty<*>> = mutableListOf(),
) : NamedEnumProperty<T>(
        propertyName,
        value,
        type,
        children,
    ),
    FilteredProperty<T>
    where T : Enum<T>, T : NamedEnum
