package net.rsprox.transcriber

import net.rsprox.transcriber.properties.Property

public interface MessageFormatter {
    public fun format(
        clientPacket: Boolean,
        cycle: Int,
        name: String,
        properties: List<Property>,
    ): String

    public fun com(
        interfaceId: Int,
        componentId: Int,
    ): String
}
