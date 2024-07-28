package net.rsprox.shared.property.filtered

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty

public class FilteredBooleanProperty(
    override val propertyName: String,
    override val value: Boolean,
    override val filterValue: Boolean,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Boolean> = Boolean::class.java,
) : ChildProperty<Boolean>,
    FilteredProperty<Boolean>
