package net.rsprox.shared.property.regular

import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.toJagCoordsText
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet

public class IdentifiedMultinpcProperty(
    override val propertyName: String,
    index: Int,
    public val baseId: Int,
    public val multinpcId: Int,
    public val npcName: String,
    level: Int,
    x: Int,
    z: Int,
) : IdentifiedChildProperty(propertyName, index, level, x, z) {
    override fun formattedValue(settings: SettingSet, dictionary: SymbolDictionary): String = buildString {
        append('(')

        if (index != Int.MIN_VALUE) {
            append("index=$index, ")
        }

        val baseSymbol = dictionary.getScriptVarTypeName(baseId, ScriptVarType.NPC)
        if (baseSymbol != null) {
            append("id=$baseSymbol")
            if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                append(" ($baseId)")
            }
        } else {
            append("id=$baseId")
        }

        append(", ")

        val multiSymbol = dictionary.getScriptVarTypeName(multinpcId, ScriptVarType.NPC)
        if (multiSymbol != null) {
            append("multinpc=$multiSymbol")
            if (settings[Setting.SHOW_IDS_AFTER_SYMBOLS]) {
                append(" ($multinpcId)")
            }
        } else if (npcName != "null") {
            append("multinpc=$npcName (id=$multinpcId)")
        } else {
            append("multinpc=$baseId")
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
