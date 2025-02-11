package net.rsprox.shared.property.regular

import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.toJagCoordsText
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet

public class IdentifiedPlayerProperty(
    override val propertyName: String,
    index: Int,
    public val playerName: String,
    level: Int,
    x: Int,
    z: Int,
) : IdentifiedChildProperty(propertyName, index, level, x, z) {
    override fun formattedValue(settings: SettingSet, dictionary: SymbolDictionary): String = buildString {
        append('(')

        if (index != Int.MIN_VALUE) {
            append("index=$index, ")
        }

        append("name=$playerName, ")

        if (settings[Setting.CONVERT_COORD_TO_JAGCOORD]) {
            val formatted = toJagCoordsText(level, x, z)
            append("coord=($formatted)")
        } else {
            append("coord=($x, $z, $level)")
        }

        append(')')
    }
}
