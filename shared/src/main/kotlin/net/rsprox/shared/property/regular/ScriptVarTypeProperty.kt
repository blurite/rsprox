package net.rsprox.shared.property.regular

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.ChildProperty

public open class ScriptVarTypeProperty<T>(
    override val type: Class<T>,
    public val scriptVarType: ScriptVarType,
    override val propertyName: String,
    override val value: T,
    override val children: MutableList<ChildProperty<*>> = mutableListOf(),
) : ChildProperty<T>
