package net.rsprox.shared.property.formatted

import net.rsprox.shared.property.ChildProperty
import java.text.NumberFormat

public class FormattedLongProperty(
    override val propertyName: String,
    override val value: Long,
    public val format: NumberFormat,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
    override val type: Class<Long> = Long::class.java,
) : ChildProperty<Long>
