package net.rsprox.transcriber.base

import net.rsprox.transcriber.MessageFormatter
import net.rsprox.transcriber.properties.Property

public open class BaseMessageFormatter : MessageFormatter {
    override fun format(
        clientPacket: Boolean,
        cycle: Int,
        name: String,
        properties: List<Property>,
    ): String {
        val builder =
            StringBuilder()
                .append('[')
                .append(cycle)
                .append(']')
                .append(if (clientPacket) " -> " else " <- ")
                .append(name)
                .append(": ")
        for (property in properties) {
            builder
                .append(property.name)
                .append("=")
                .append(property.value)
                .append(", ")
        }
        // Delete the trailing comma from the last property, if it exists
        if (properties.isNotEmpty()) {
            builder.delete(builder.length - 2, builder.length)
        }
        return builder.toString()
    }

    override fun com(
        interfaceId: Int,
        componentId: Int,
    ): String {
        return "$interfaceId:$componentId"
    }
}
