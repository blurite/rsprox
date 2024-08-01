package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty

public class IdentifiedNpcProperty(
    override val propertyName: String,
    public val index: Int,
    public val id: Int,
    public val npcName: String,
    public val level: Int,
    public val x: Int,
    public val z: Int,
    override val value: String =
        if (index == Int.MIN_VALUE) {
            "(name=$npcName, coord=($x, $z, $level))"
        } else {
            "(index=$index, name=$npcName, coord=($x, $z, $level))"
        },
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<String> = String::class.java,
) : ChildProperty<String>
