package net.rsprox.shared.property

import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.filtered.FilteredAnyProperty
import net.rsprox.shared.property.filtered.FilteredNamedEnumProperty
import net.rsprox.shared.property.filtered.FilteredScriptVarTypeProperty
import net.rsprox.shared.property.formatted.FormattedIntProperty
import net.rsprox.shared.property.regular.AnyProperty
import net.rsprox.shared.property.regular.EnumProperty
import net.rsprox.shared.property.regular.GroupProperty
import net.rsprox.shared.property.regular.IdentifiedMultinpcProperty
import net.rsprox.shared.property.regular.IdentifiedNpcProperty
import net.rsprox.shared.property.regular.IdentifiedPlayerProperty
import net.rsprox.shared.property.regular.IdentifiedWorldEntityProperty
import net.rsprox.shared.property.regular.ListProperty
import net.rsprox.shared.property.regular.NamedEnumProperty
import net.rsprox.shared.property.regular.ScriptProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.regular.ShortNpcProperty
import net.rsprox.shared.property.regular.ShortPlayerProperty
import net.rsprox.shared.property.regular.UnidentifiedNpcProperty
import net.rsprox.shared.property.regular.UnidentifiedPlayerProperty
import net.rsprox.shared.property.regular.UnidentifiedWorldEntityProperty
import net.rsprox.shared.property.regular.VarbitProperty
import net.rsprox.shared.property.regular.VarpProperty
import net.rsprox.shared.property.regular.ZoneCoordProperty
import java.text.NumberFormat
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@PublishedApi
internal inline fun <reified T> type(): Class<T> {
    return T::class.java
}

public fun ChildProperty<*>.isExcluded(): Boolean {
    if (this !is FilteredProperty<*>) {
        return false
    }
    return value == filterValue
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

@OptIn(ExperimentalContracts::class)
public fun Property.list(
    name: String,
    builderAction: ListProperty.() -> Unit,
): ListProperty {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    val group = ListProperty(name)
    builderAction(group)
    return child(group)
}

public fun Property.com(
    id: Int,
    com: Int,
): ScriptVarTypeProperty<*> {
    return com("com", id, com)
}

public fun Property.com(
    name: String,
    id: Int,
    com: Int,
): ScriptVarTypeProperty<*> {
    if (id and 0xFFFF != 0xFFFF && com and 0xFFFF == 0xFFFF) {
        return scriptVarType(name, ScriptVarType.INTERFACE, id)
    }
    return scriptVarType(name, ScriptVarType.COMPONENT, (id and 0xFFFF shl 16) or (com and 0xFFFF))
}

public fun Property.inter(id: Int): ScriptVarTypeProperty<*> {
    return inter("id", id)
}

public fun Property.inter(
    name: String,
    id: Int,
): ScriptVarTypeProperty<*> {
    return scriptVarType(name, ScriptVarType.INTERFACE, id)
}

public fun Property.coordGridProperty(
    level: Int,
    x: Int,
    z: Int,
    name: String = "coord",
): ScriptVarTypeProperty<*> {
    return scriptVarType(
        name,
        ScriptVarType.COORDGRID,
        (level and 0x3 shl 28) or (x and 0x3FFF shl 14) or (z and 0x3FFF),
    )
}

public fun Property.zoneCoordGrid(
    level: Int,
    zoneX: Int,
    zoneZ: Int,
    name: String = "zone",
): ZoneCoordProperty {
    return child(
        ZoneCoordProperty(
            name,
            level,
            zoneX,
            zoneZ,
        ),
    )
}

public fun Property.string(
    name: String,
    value: String?,
): ScriptVarTypeProperty<*> {
    return scriptVarType(name, ScriptVarType.STRING, value)
}

public fun Property.filteredString(
    name: String,
    value: String?,
    filterValue: String?,
): FilteredScriptVarTypeProperty<*> {
    return filteredScriptVarType(name, ScriptVarType.STRING, value, filterValue)
}

public fun Property.script(
    name: String,
    value: Int,
): ScriptProperty {
    return child(
        ScriptProperty(
            name,
            value,
        ),
    )
}

public fun Property.varp(
    name: String,
    value: Int,
): VarpProperty {
    return child(
        VarpProperty(
            name,
            value,
        ),
    )
}

public fun Property.varbit(
    name: String,
    value: Int,
): VarbitProperty {
    return child(
        VarbitProperty(
            name,
            value,
        ),
    )
}

public fun Property.int(
    name: String,
    value: Int,
): ScriptVarTypeProperty<*> {
    return scriptVarType(name, ScriptVarType.INT, value)
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
): FilteredScriptVarTypeProperty<*> {
    return filteredScriptVarType(name, ScriptVarType.INT, value, filterValue)
}

public fun Property.long(
    name: String,
    value: Long,
): ScriptVarTypeProperty<*> {
    return scriptVarType(name, ScriptVarType.LONG, value)
}

public fun Property.filteredLong(
    name: String,
    value: Long,
    filterValue: Long,
): FilteredScriptVarTypeProperty<*> {
    return filteredScriptVarType(name, ScriptVarType.LONG, value, filterValue)
}

public fun Property.boolean(
    name: String,
    value: Boolean,
): ScriptVarTypeProperty<*> {
    return scriptVarType(name, ScriptVarType.BOOLEAN, if (value) 1 else 0)
}

public fun Property.filteredBoolean(
    name: String,
    value: Boolean,
    filteredValue: Boolean = false,
): FilteredScriptVarTypeProperty<*> {
    return filteredScriptVarType(name, ScriptVarType.BOOLEAN, if (value) 1 else 0, if (filteredValue) 1 else 0)
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
                        type(),
                        type,
                        name,
                        value as? String?,
                    ),
                )
            }
        }
    @Suppress("UNCHECKED_CAST")
    return result as ScriptVarTypeProperty<V>
}

public fun <V> createScriptVarType(
    name: String,
    type: ScriptVarType,
    value: V,
): ScriptVarTypeProperty<V> {
    val result =
        when (type.baseVarType) {
            BaseVarType.INTEGER -> {
                ScriptVarTypeProperty(
                    Int::class.java,
                    type,
                    name,
                    value as Int,
                )
            }

            BaseVarType.LONG -> {
                ScriptVarTypeProperty(
                    Long::class.java,
                    type,
                    name,
                    value as Long,
                )
            }

            BaseVarType.STRING -> {
                ScriptVarTypeProperty(
                    type(),
                    type,
                    name,
                    value as? String?,
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
                        type(),
                        type,
                        name,
                        value as? String?,
                        filterValue as? String?,
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
): EnumProperty<T> where T : Enum<T> {
    return child(
        EnumProperty(
            name,
            value,
            T::class.java,
        ),
    )
}

public inline fun <reified T> Property.any(
    name: String,
    value: T?,
): AnyProperty<T?> {
    return child(
        AnyProperty(
            name,
            value,
            type(),
        ),
    )
}

public inline fun <reified T> Property.filteredAny(
    name: String,
    value: T?,
    filterValue: T?,
): FilteredAnyProperty<T?> {
    return child(
        FilteredAnyProperty(
            name,
            value,
            type(),
            filterValue,
        ),
    )
}

public inline fun <reified T> Property.namedEnum(
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

public inline fun <reified T> Property.filteredNamedEnum(
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
    id: Int,
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
            id,
            name,
            level,
            x,
            z,
        ),
    )
}

public fun Property.identifiedMultinpc(
    index: Int,
    baseId: Int,
    multinpc: Int,
    name: String,
    level: Int,
    x: Int,
    z: Int,
    propertyName: String = "npc",
): IdentifiedMultinpcProperty {
    return child(
        IdentifiedMultinpcProperty(
            propertyName,
            index,
            baseId,
            multinpc,
            name,
            level,
            x,
            z,
        ),
    )
}

public fun Property.shortNpc(
    index: Int,
    id: Int,
    propertyName: String = "npc",
): ShortNpcProperty {
    return child(
        ShortNpcProperty(
            propertyName,
            index,
            id,
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

public fun Property.shortPlayer(
    index: Int,
    name: String?,
    propertyName: String = "player",
): ShortPlayerProperty {
    return child(
        ShortPlayerProperty(
            propertyName,
            index,
            name,
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

public fun Property.identifiedWorldEntity(
    index: Int,
    level: Int,
    x: Int,
    z: Int,
    sizeX: Int,
    sizeZ: Int,
    centerFineOffsetX: Int?,
    centerFineOffsetZ: Int?,
    propertyName: String = "worldentity",
): IdentifiedWorldEntityProperty {
    return child(
        IdentifiedWorldEntityProperty(
            propertyName,
            index,
            level,
            x,
            z,
            sizeX,
            sizeZ,
            centerFineOffsetX,
            centerFineOffsetZ,
        ),
    )
}

public fun Property.unidentifiedWorldEntity(
    index: Int,
    propertyName: String = "worldentity",
): UnidentifiedWorldEntityProperty {
    return child(
        UnidentifiedWorldEntityProperty(
            propertyName,
            index,
        ),
    )
}
