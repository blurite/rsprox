package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class IdentifiedNpcProperty(
    override val propertyName: String,
    public val index: Int,
    public val npcName: String,
    public val level: Int,
    public val x: Int,
    public val z: Int,
    override val value: String = "(index=$index, name=$npcName, ($x, $z, $level))",
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String>
