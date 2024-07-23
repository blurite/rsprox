package net.rsprox.transcriber.base

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.transcriber.MessageFormatter
import net.rsprox.transcriber.SINGLE_INDENTATION
import net.rsprox.transcriber.ScriptVarType
import net.rsprox.transcriber.properties.Property

public open class BaseMessageFormatter : MessageFormatter {
    override fun format(
        clientPacket: Boolean,
        name: String,
        properties: List<Property>,
        indentation: Int,
    ): String {
        val builder = StringBuilder()
        val prefix = SINGLE_INDENTATION.repeat(indentation)
        builder.append(prefix)
        val wrapped = indentation >= 2
        if (wrapped) {
            if (name.isNotEmpty()) {
                builder.append(name)
                if (properties.isNotEmpty()) {
                    builder.append('(')
                }
            }
        } else {
            if (name.isNotEmpty()) {
                builder.append('[').append(name).append("] ")
            }
        }
        for (property in properties) {
            builder
                .append(property.name)
                .append('=')
                .append(formatProperty(property.value))
                .append(", ")
        }
        // Delete the trailing comma from the last property, if it exists
        if (properties.isNotEmpty()) {
            builder.delete(builder.length - 2, builder.length)
        }
        if (wrapped && name.isNotEmpty() && properties.isNotEmpty()) {
            builder.append(')')
        }
        return builder.toString()
    }

    private fun formatProperty(property: Any): String {
        return when (property) {
            is CoordGrid -> {
                coord(property.level, property.x, property.z)
            }
            else -> {
                property.toString()
            }
        }
    }

    override fun com(
        interfaceId: Int,
        componentId: Int,
    ): String {
        val int = if (interfaceId == 0xFFFF) -1 else interfaceId
        val com = if (componentId == 0xFFFF) -1 else componentId
        if (int == -1 && com == -1) {
            return "null"
        }
        return if (int != -1 && com == -1) {
            "$int"
        } else {
            "$int:$com"
        }
    }

    override fun coord(
        level: Int,
        x: Int,
        z: Int,
    ): String {
        if (level == 15 && x == 16383 && z == 16383) {
            return "null"
        }
        return "($x, $z, $level)"
    }

    override fun zoneCoord(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
    ): String {
        return "($zoneX, $zoneZ, $level)"
    }

    override fun script(id: Int): String {
        return "$id"
    }

    override fun type(
        type: ScriptVarType,
        id: Int,
    ): String {
        return when (type) {
            ScriptVarType.COLOUR -> {
                val red = id ushr 10 and 0x1F
                val green = id ushr 5 and 0x1F
                val blue = id and 0x1F
                "(red=$red, green=$green, blue=$blue)"
            }
            ScriptVarType.COMPONENT -> {
                com(id ushr 16, id and 0xFFFF)
            }
            ScriptVarType.COORDGRID -> {
                coord(id ushr 28, id ushr 14 and 0x3FFF, id and 0x3FFF)
            }
            else -> {
                "$id"
            }
        }
    }

    override fun varp(id: Int): String {
        return "$id"
    }

    override fun varbit(id: Int): String {
        return "$id"
    }
}
