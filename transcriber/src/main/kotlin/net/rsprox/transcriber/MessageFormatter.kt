package net.rsprox.transcriber

import net.rsprox.transcriber.properties.Property

public interface MessageFormatter {
    public fun format(
        clientPacket: Boolean,
        name: String,
        properties: List<Property>,
        indentation: Int,
    ): String

    public fun com(
        interfaceId: Int,
        componentId: Int,
    ): String

    public fun coord(
        level: Int,
        x: Int,
        z: Int,
    ): String

    public fun type(
        type: ScriptVarType,
        id: Int,
    ): String
}
