package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class BooleanProperty(
    override val propertyName: String,
    override val value: Boolean,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Boolean> = Boolean::class.java,
) : ChildProperty<Boolean>
