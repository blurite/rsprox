package net.rsprox.shared.property

import net.rsprox.shared.BaseVarType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.filtered.FilteredNamedEnumProperty
import net.rsprox.shared.property.filtered.FilteredScriptVarTypeProperty
import net.rsprox.shared.property.formatted.FormattedIntProperty
import net.rsprox.shared.property.regular.EnumProperty
import net.rsprox.shared.property.regular.IdentifiedMultinpcProperty
import net.rsprox.shared.property.regular.IdentifiedNpcProperty
import net.rsprox.shared.property.regular.NamedEnumProperty
import net.rsprox.shared.property.regular.ScriptProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.regular.ShortNpcProperty
import net.rsprox.shared.property.regular.VarbitProperty
import net.rsprox.shared.property.regular.VarpProperty
import net.rsprox.shared.property.regular.ZoneCoordProperty
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore

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
        public fun default(
            dictionary: SymbolDictionary,
            settingStore: SettingSetStore,
        ): PropertyFormatterCollection {
            val settings = settingStore.getActive()
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
                val symbol = dictionary.getScriptVarTypeName(it.id, ScriptVarType.NPC)
                val col =
                    when {
                        symbol != null && settings[Setting.SHOW_IDS_AFTER_SYMBOLS] -> "id=$symbol (${it.id})"
                        symbol != null -> "id=$symbol"
                        it.npcName != "null" -> it.npcName + " (id=${it.id})"
                        else -> "id=${it.id}"
                    }
                if (it.index == Int.MIN_VALUE) {
                    "($col, coord=(${it.x}, ${it.z}, ${it.level}))"
                } else {
                    "(index=${it.index}, $col, coord=(${it.x}, ${it.z}, ${it.level}))"
                }
            }
            builder.add<IdentifiedMultinpcProperty> {
                val base = dictionary.getScriptVarTypeName(it.baseId, ScriptVarType.NPC)
                val baseCol =
                    when {
                        base != null && settings[Setting.SHOW_IDS_AFTER_SYMBOLS] -> "id=$base (${it.baseId})"
                        base != null -> "id=$base"
                        else -> "id=${it.baseId}"
                    }
                val multinpc = dictionary.getScriptVarTypeName(it.multinpcId, ScriptVarType.NPC)
                val multinpcCol =
                    when {
                        multinpc != null && settings[Setting.SHOW_IDS_AFTER_SYMBOLS] -> {
                            "multinpc=$multinpc (${it.multinpcId})"
                        }
                        multinpc != null -> "multinpc=$multinpc"
                        it.npcName != "null" -> "multinpc=${it.npcName}"
                        else -> "multinpc=${it.multinpcId}"
                    }
                if (it.index == Int.MIN_VALUE) {
                    "($baseCol, $multinpcCol, coord=(${it.x}, ${it.z}, ${it.level}))"
                } else {
                    "(index=${it.index}, $baseCol, $multinpcCol, coord=(${it.x}, ${it.z}, ${it.level}))"
                }
            }
            builder.add<ShortNpcProperty> {
                val symbol = dictionary.getScriptVarTypeName(it.id, ScriptVarType.NPC) ?: return@add "(id=${it.id})"
                if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                    "$symbol (${it.id})"
                } else {
                    symbol
                }
            }
            builder.add<ScriptProperty> {
                val symbol = dictionary.getScriptName(it.value) ?: return@add "${it.value}"
                if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                    "$symbol (${it.value})"
                } else {
                    symbol
                }
            }
            builder.add<VarbitProperty> {
                val symbol = dictionary.getVarbitName(it.value) ?: return@add "${it.value}"
                if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                    "$symbol (${it.value})"
                } else {
                    symbol
                }
            }
            builder.add<VarpProperty> {
                val symbol = dictionary.getVarpName(it.value) ?: return@add "${it.value}"
                if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                    "$symbol (${it.value})"
                } else {
                    symbol
                }
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
                        return@PropertyFormatter if (settings[Setting.PREFER_SINGLE_QUOTE_ON_STRINGS]) {
                            "'${it.value}'"
                        } else {
                            "\"${it.value}\""
                        }
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
                            var x = value ushr 14 and 0x3FFF
                            if (x == 0x3FFF) x = -1
                            var z = value and 0x3FFF
                            if (z == 0x3FFF) z = -1
                            "($x, $z, $level)"
                        }
                        ScriptVarType.COMPONENT -> {
                            var interfaceId = value ushr 16 and 0xFFFF
                            var componentId = value and 0xFFFF
                            if (interfaceId == 0xFFFF) interfaceId = -1
                            if (componentId == 0xFFFF) componentId = -1
                            val name = dictionary.getScriptVarTypeName(value, ScriptVarType.COMPONENT)
                            if (name != null) {
                                return@PropertyFormatter if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                                    "$name ($interfaceId:$componentId)"
                                } else {
                                    name
                                }
                            }
                            val interfaceName =
                                dictionary.getScriptVarTypeName(
                                    interfaceId,
                                    ScriptVarType.INTERFACE,
                                )
                            when {
                                interfaceName != null && settings[Setting.SHOW_IDS_AFTER_SYMBOLS] -> {
                                    "$interfaceName:$componentId ($interfaceId:$componentId)"
                                }
                                interfaceName != null -> {
                                    "$interfaceName:$componentId"
                                }
                                else -> {
                                    "$interfaceId:$componentId"
                                }
                            }
                        }
                        ScriptVarType.BOOLEAN -> {
                            if (value == 1) "true" else "false"
                        }
                        else -> {
                            val symbol =
                                dictionary.getScriptVarTypeName(value, it.scriptVarType)
                                    ?: return@PropertyFormatter "$value"
                            return@PropertyFormatter if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                                "$symbol ($value)"
                            } else {
                                symbol
                            }
                        }
                    }
                }
            builder.add<ScriptVarTypeProperty<*>>(scriptVarTypePropertyFormatter)
            builder.add<FilteredScriptVarTypeProperty<*>>(scriptVarTypePropertyFormatter)
            return builder.build()
        }
    }
}
