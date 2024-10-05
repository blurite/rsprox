package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class AnyProperty<T>(
    override val propertyName: String,
    override val value: T,
    override val type: Class<T>,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
) : ChildProperty<T>
