package net.rsprox.shared.property.regular

import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.NopSymbolDictionary
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.settings.NopSettingSet
import net.rsprox.shared.settings.SettingSet

public sealed class IdentifiedChildProperty(
    override val propertyName: String,
    public val index: Int,
    public val level: Int,
    public val x: Int,
    public val z: Int,
) : ChildProperty<String> {
    override val children: MutableList<ChildProperty<*>> = mutableListOf()

    override val type: Class<String> = String::class.java

    override val value: String
        get() = formattedValue(NopSettingSet, NopSymbolDictionary)

    public abstract fun formattedValue(
        settings: SettingSet,
        dictionary: SymbolDictionary,
    ): String
}
