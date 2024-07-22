package net.rsprox.transcriber.base

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.transcriber.MessageFormatter
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
        val prefix = "  ".repeat(indentation)
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
        return "$interfaceId:$componentId"
    }

    override fun coord(
        level: Int,
        x: Int,
        z: Int,
    ): String {
        return "($x, $z, $level)"
    }

    override fun type(
        type: ScriptVarType,
        id: Int,
    ): String {
        return "$id"
    }
}
