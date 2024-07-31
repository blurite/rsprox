package net.rsprox.shared.property.filtered

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.FilteredProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty

public class FilteredScriptVarTypeProperty<T>(
    type: Class<T>,
    scriptVarType: ScriptVarType,
    propertyName: String,
    value: T,
    override val filterValue: T,
    children: MutableList<ChildProperty<*>> = mutableListOf(),
) : ScriptVarTypeProperty<T>(
        type,
        scriptVarType,
        propertyName,
        value,
        children,
    ),
    FilteredProperty<T>
