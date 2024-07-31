package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class UnidentifiedWorldEntityProperty(
    override val propertyName: String,
    public val index: Int,
    override val value: String = "(index=$index)",
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String>
