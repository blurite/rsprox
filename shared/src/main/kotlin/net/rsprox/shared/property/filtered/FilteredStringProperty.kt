package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty
import net.rsprox.shared.property.type

public class FilteredStringProperty(
    override val propertyName: String,
    override val value: String?,
    override val filterValue: String?,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String?> = type(),
) : ChildProperty<String?>,
    FilteredProperty<String?>
