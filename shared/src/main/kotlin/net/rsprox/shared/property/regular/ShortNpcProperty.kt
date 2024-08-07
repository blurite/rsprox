package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class ShortNpcProperty(
    override val propertyName: String,
    public val index: Int,
    public val id: Int,
    override val value: String = index.toString(),
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String>
