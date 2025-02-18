package net.rsprox.shared.property.regular

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.toJagCoordsText
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet

public class IdentifiedNpcProperty(
    override val propertyName: String,
    index: Int,
    public val id: Int,
    public val npcName: String,
    level: Int,
    x: Int,
    z: Int,
) : IdentifiedChildProperty(propertyName, index, level, x, z) {
    override fun formattedValue(
        settings: SettingSet,
        dictionary: SymbolDictionary,
    ): String =
        buildString {
            append('(')

            if (index != Int.MIN_VALUE) {
                append("index=$index, ")
            }

            val symbol = dictionary.getScriptVarTypeName(id, ScriptVarType.NPC)
            if (symbol != null) {
                append("id=$symbol")
                if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                    append(" ($id)")
                }
            } else if (npcName != "null") {
                append("$npcName (id=$id)")
            } else {
                append("id=$id")
            }

            append(", ")

            if (settings[Setting.CONVERT_COORD_TO_JAGCOORD]) {
                val formatted = toJagCoordsText(level, x, z)
                append("coord=($formatted)")
            } else {
                append("coord=($x, $z, $level)")
            }

            append(')')
        }
}
