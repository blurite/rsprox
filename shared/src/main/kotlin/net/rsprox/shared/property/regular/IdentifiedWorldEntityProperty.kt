package net.rsprox.shared.property.regular

import net.rsprox.shared.property.SymbolDictionary
import net.rsprox.shared.property.toJagCoordsText
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSet

public class IdentifiedWorldEntityProperty(
    override val propertyName: String,
    index: Int,
    private val id: Int,
    level: Int,
    x: Int,
    z: Int,
    public val sizeX: Int,
    public val sizeZ: Int,
    public val centerFineOffsetX: Int?,
    public val centerFineOffsetZ: Int?,
) : IdentifiedChildProperty(propertyName, index, level, x, z) {
    override fun formattedValue(
        settings: SettingSet,
        dictionary: SymbolDictionary,
    ): String =
        buildString {
            append('(')

            append("index=$index, ")

            if (id != -1) {
                append("id=$id, ")
            }

            if (settings[Setting.CONVERT_COORD_TO_JAGCOORD]) {
                val formatted = toJagCoordsText(level, x, z)
                append("coord=($formatted)")
            } else {
                append("coord=($x, $z, $level)")
            }

            append(", sizex=$sizeX, sizez=$sizeZ")

            if (centerFineOffsetX != null && centerFineOffsetZ != null) {
                append(", offsetx=$centerFineOffsetX, offsetz=$centerFineOffsetZ")
            }

            append(')')
        }
}
