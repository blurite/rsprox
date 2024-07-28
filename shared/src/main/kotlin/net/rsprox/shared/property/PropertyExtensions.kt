package net.rsprox.shared.property

import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.filtered.FilteredBooleanProperty
import net.rsprox.shared.property.filtered.FilteredIntProperty
import net.rsprox.shared.property.filtered.FilteredNamedEnumProperty
import net.rsprox.shared.property.filtered.FilteredScriptVarTypeProperty
import net.rsprox.shared.property.filtered.FilteredStringProperty
import net.rsprox.shared.property.formatted.FormattedIntProperty
import net.rsprox.shared.property.regular.BooleanProperty
import net.rsprox.shared.property.regular.ComponentProperty
import net.rsprox.shared.property.regular.CoordGridProperty
import net.rsprox.shared.property.regular.GroupProperty
import net.rsprox.shared.property.regular.IdentifiedNpcProperty
import net.rsprox.shared.property.regular.IdentifiedPlayerProperty
import net.rsprox.shared.property.regular.IntProperty
import net.rsprox.shared.property.regular.LongProperty
import net.rsprox.shared.property.regular.NamedEnumProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.regular.StringProperty
import net.rsprox.shared.property.regular.UnidentifiedNpcProperty
import net.rsprox.shared.property.regular.UnidentifiedPlayerProperty
import java.text.NumberFormat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun <reified T> type(): Class<T> {
    return T::class.java
}

@OptIn(ExperimentalContracts::class)
public fun Property.group(
    name: String = "",
    builderAction: GroupProperty.() -> Unit,
): GroupProperty {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val group = GroupProperty(name)
    builderAction(group)
    return child(group)
}

public fun Property.com(
    id: Int,
    com: Int,
): ComponentProperty {
    return com("com", id, com)
}

public fun Property.com(
    name: String,
    id: Int,
    com: Int,
): ComponentProperty {
    return child(
        ComponentProperty(
            name,
            id,
            com,
        ),
    )
}

public fun Property.coordGrid(
    level: Int,
    x: Int,
    z: Int,
    name: String = "coord",
): CoordGridProperty {
    return child(
        CoordGridProperty(
            name,
            level,
            x,
            z,
        ),
    )
}

public fun Property.string(
    name: String,
    value: String?,
): StringProperty {
    return child(
        StringProperty(
            name,
            value,
        ),
    )
}

public fun Property.filteredString(
    name: String,
    value: String?,
    filterValue: String?,
): FilteredStringProperty {
    return child(
        FilteredStringProperty(
            name,
            value,
            filterValue,
        ),
    )
}

public fun Property.int(
    name: String,
    value: Int,
): IntProperty {
    return child(
        IntProperty(
            name,
            value,
        ),
    )
}

public fun Property.formattedInt(
    name: String,
    value: Int,
    format: NumberFormat = NumberFormat.getIntegerInstance(),
): FormattedIntProperty {
    return child(
        FormattedIntProperty(
            name,
            value,
            format,
        ),
    )
}

public fun Property.filteredInt(
    name: String,
    value: Int,
    filterValue: Int,
): FilteredIntProperty {
    return child(
        FilteredIntProperty(
            name,
            value,
            filterValue,
        ),
    )
}

public fun Property.long(
    name: String,
    value: Long,
): LongProperty {
    return child(
        LongProperty(
            name,
            value,
        ),
    )
}

public fun Property.boolean(
    name: String,
    value: Boolean,
): BooleanProperty {
    return child(
        BooleanProperty(
            name,
            value,
        ),
    )
}

public fun Property.filteredBoolean(
    name: String,
    value: Boolean,
    filteredValue: Boolean = false,
): FilteredBooleanProperty {
    return child(
        FilteredBooleanProperty(
            name,
            value,
            filteredValue,
        ),
    )
}

public fun <V> Property.scriptVarType(
    name: String,
    type: ScriptVarType,
    value: V,
): ScriptVarTypeProperty<V> {
    val result =
        when (type.baseVarType) {
            BaseVarType.INTEGER -> {
                child(
                    ScriptVarTypeProperty(
                        Int::class.java,
                        type,
                        name,
                        value as Int,
                    ),
                )
            }
            BaseVarType.LONG -> {
                child(
                    ScriptVarTypeProperty(
                        Long::class.java,
                        type,
                        name,
                        value as Long,
                    ),
                )
            }
            BaseVarType.STRING -> {
                child(
                    ScriptVarTypeProperty(
                        String::class.java,
                        type,
                        name,
                        value as String,
                    ),
                )
            }
        }
    @Suppress("UNCHECKED_CAST")
    return result as ScriptVarTypeProperty<V>
}

public fun <V> Property.filteredScriptVarType(
    name: String,
    type: ScriptVarType,
    value: V,
    filterValue: V,
): FilteredScriptVarTypeProperty<V> {
    val result =
        when (type.baseVarType) {
            BaseVarType.INTEGER -> {
                child(
                    FilteredScriptVarTypeProperty(
                        Int::class.java,
                        type,
                        name,
                        value as Int,
                        filterValue as Int,
                    ),
                )
            }
            BaseVarType.LONG -> {
                child(
                    FilteredScriptVarTypeProperty(
                        Long::class.java,
                        type,
                        name,
                        value as Long,
                        filterValue as Long,
                    ),
                )
            }
            BaseVarType.STRING -> {
                child(
                    FilteredScriptVarTypeProperty(
                        String::class.java,
                        type,
                        name,
                        value as String,
                        filterValue as String,
                    ),
                )
            }
        }
    @Suppress("UNCHECKED_CAST")
    return result as FilteredScriptVarTypeProperty<V>
}

public inline fun <reified T> Property.enum(
    name: String,
    value: T,
): NamedEnumProperty<T> where T : Enum<T>, T : NamedEnum {
    return child(
        NamedEnumProperty(
            name,
            value,
            T::class.java,
        ),
    )
}

public inline fun <reified T> Property.filteredEnum(
    name: String,
    value: T,
    filterValue: T,
): FilteredNamedEnumProperty<T> where T : Enum<T>, T : NamedEnum {
    return child(
        FilteredNamedEnumProperty(
            name,
            value,
            filterValue,
            T::class.java,
        ),
    )
}

public fun Property.identifiedNpc(
    index: Int,
    name: String,
    level: Int,
    x: Int,
    z: Int,
    propertyName: String = "npc",
): IdentifiedNpcProperty {
    return child(
        IdentifiedNpcProperty(
            propertyName,
            index,
            name,
            level,
            x,
            z,
        ),
    )
}

public fun Property.unidentifiedNpc(
    index: Int,
    propertyName: String = "npc",
): UnidentifiedNpcProperty {
    return child(
        UnidentifiedNpcProperty(
            propertyName,
            index,
        ),
    )
}

public fun Property.identifiedPlayer(
    index: Int,
    name: String,
    level: Int,
    x: Int,
    z: Int,
    propertyName: String = "player",
): IdentifiedPlayerProperty {
    return child(
        IdentifiedPlayerProperty(
            propertyName,
            index,
            name,
            level,
            x,
            z,
        ),
    )
}

public fun Property.unidentifiedPlayer(
    index: Int,
    propertyName: String = "player",
): UnidentifiedPlayerProperty {
    return child(
        UnidentifiedPlayerProperty(
            propertyName,
            index,
        ),
    )
}
