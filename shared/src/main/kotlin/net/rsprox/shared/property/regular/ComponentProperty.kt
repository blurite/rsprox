package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

@Suppress("MemberVisibilityCanBePrivate")
public class ComponentProperty(
    override val propertyName: String,
    public val interfaceId: Int,
    public val componentId: Int,
    override val value: Int = (interfaceId and 0xFFFF shl 16) or (componentId and 0xFFFF),
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Int> = Int::class.java,
) : ChildProperty<Int>
