package net.rsprox.shared.property

import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.filtered.FilteredNamedEnumProperty
import net.rsprox.shared.property.filtered.FilteredScriptVarTypeProperty
import net.rsprox.shared.property.formatted.FormattedIntProperty
import net.rsprox.shared.property.regular.EnumProperty
import net.rsprox.shared.property.regular.IdentifiedNpcProperty
import net.rsprox.shared.property.regular.NamedEnumProperty
import net.rsprox.shared.property.regular.ScriptProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.regular.VarbitProperty
import net.rsprox.shared.property.regular.VarpProperty
import net.rsprox.shared.property.regular.ZoneCoordProperty

public class PropertyFormatterCollection private constructor(
    private val formatters: Map<Class<*>, PropertyFormatter<*>>,
) {
    public fun getFormatter(clazz: Class<*>): PropertyFormatter<*>? {
        return formatters[clazz]
    }

    public inline fun <reified T : Property> getTypedFormatter(clazz: Class<T>): PropertyFormatter<T>? {
        @Suppress("UNCHECKED_CAST")
        return getFormatter(clazz) as? PropertyFormatter<T>?
    }

    public class Builder {
        private val formatters: MutableMap<Class<*>, PropertyFormatter<*>> = mutableMapOf()

        public fun <T : Property> add(
            clazz: Class<T>,
            value: PropertyFormatter<T>,
        ): Builder {
            val old = this.formatters.put(clazz, value)
            require(old == null) {
                "Overwriting existing formatter: $clazz, $value, $old"
            }
            return this
        }

        public inline fun <reified T : Property> add(value: PropertyFormatter<T>): Builder {
            return add(T::class.java, value)
        }

        public fun build(): PropertyFormatterCollection {
            return PropertyFormatterCollection(formatters.toMap())
        }
    }

    public companion object {
        public fun default(dictionary: SymbolDictionary): PropertyFormatterCollection {
            val builder = Builder()
            val enumPropertyFormatter =
                PropertyFormatter<NamedEnumProperty<*>> {
                    (it.value as NamedEnum).prettyName
                }
            builder.add<NamedEnumProperty<*>>(enumPropertyFormatter)
            builder.add<FilteredNamedEnumProperty<*>>(enumPropertyFormatter)
            builder.add<EnumProperty<*>> {
                it.value.toString().lowercase()
            }
            builder.add<IdentifiedNpcProperty> {
                val id = dictionary.getScriptVarTypeName(it.id, ScriptVarType.NPC)
                val col = if (id != null) "id=$id" else "name=${it.npcName}"
                if (it.index == Int.MIN_VALUE) {
                    "($col, coord=(${it.x}, ${it.z}, ${it.level}))"
                } else {
                    "(index=${it.index}, $col, coord=(${it.x}, ${it.z}, ${it.level}))"
                }
            }
            builder.add<ScriptProperty> {
                dictionary.getScriptName(it.value) ?: "${it.value}"
            }
            builder.add<VarbitProperty> {
                dictionary.getVarbitName(it.value) ?: "${it.value}"
            }
            builder.add<VarpProperty> {
                dictionary.getVarpName(it.value) ?: "${it.value}"
            }
            builder.add<ZoneCoordProperty> {
                "(${it.zoneX}, ${it.zoneZ}, ${it.level})"
            }
            builder.add<FormattedIntProperty> {
                it.format.format(it.value)
            }
            val scriptVarTypePropertyFormatter =
                PropertyFormatter<ScriptVarTypeProperty<*>> {
                    val vartype = it.scriptVarType.baseVarType
                    if (vartype == BaseVarType.STRING) {
                        return@PropertyFormatter "'${it.value}'"
                    }
                    if (vartype != BaseVarType.INTEGER) {
                        return@PropertyFormatter it.value.toString()
                    }
                    val value = it.value as Int
                    when (it.scriptVarType) {
                        ScriptVarType.COORDGRID -> {
                            if (value == -1) {
                                return@PropertyFormatter "null"
                            }
                            val level = value ushr 28
                            val x = value ushr 14 and 0x3FFF
                            val z = value and 0x3FFF
                            "($x, $z, $level)"
                        }
                        ScriptVarType.COMPONENT -> {
                            val name = dictionary.getScriptVarTypeName(value, ScriptVarType.COMPONENT)
                            if (name != null) {
                                return@PropertyFormatter name
                            }
                            var interfaceId = value ushr 16 and 0xFFFF
                            var componentId = value and 0xFFFF
                            if (interfaceId == 0xFFFF) interfaceId = -1
                            if (componentId == 0xFFFF) componentId = -1
                            val interfaceName =
                                dictionary.getScriptVarTypeName(
                                    interfaceId,
                                    ScriptVarType.INTERFACE,
                                )
                            if (interfaceName != null) {
                                "$interfaceName:$componentId"
                            } else {
                                "$interfaceId:$componentId"
                            }
                        }
                        ScriptVarType.BOOLEAN -> {
                            if (value == 1) "true" else "false"
                        }
                        else -> {
                            dictionary.getScriptVarTypeName(value, it.scriptVarType) ?: "$value"
                        }
                    }
                }
            builder.add<ScriptVarTypeProperty<*>>(scriptVarTypePropertyFormatter)
            builder.add<FilteredScriptVarTypeProperty<*>>(scriptVarTypePropertyFormatter)
            return builder.build()
        }
    }
}
