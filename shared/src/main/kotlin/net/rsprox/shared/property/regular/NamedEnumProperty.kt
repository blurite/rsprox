package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.NamedEnum

public class NamedEnumProperty<T>(
    override val propertyName: String,
    override val value: T,
    override val type: Class<T>,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
) : ChildProperty<T> where T : Enum<T>, T : NamedEnum
