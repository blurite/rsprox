package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class LongProperty(
    override val propertyName: String,
    override val value: Long,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Long> = Long::class.java,
) : ChildProperty<Long>
