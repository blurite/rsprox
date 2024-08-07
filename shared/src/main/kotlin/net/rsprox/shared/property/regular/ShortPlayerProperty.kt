package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class ShortPlayerProperty(
    override val propertyName: String,
    public val index: Int,
    private val playerName: String?,
    override val value: String = playerName ?: "(index=$index)",
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String>
