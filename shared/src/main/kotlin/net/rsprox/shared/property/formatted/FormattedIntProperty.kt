package net.rsprox.shared.property.formatted

import net.rsprox.shared.property.ChildProperty
import java.text.NumberFormat

public class FormattedIntProperty(
    override val propertyName: String,
    override val value: Int,
    public val format: NumberFormat,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Int> = Int::class.java,
) : ChildProperty<Int>
