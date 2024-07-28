package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.type

public class StringProperty(
    override val propertyName: String,
    override val value: String?,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String?> = type(),
) : ChildProperty<String?>
