package net.rsprox.transcriber

import net.rsprox.shared.ScriptVarType
import net.rsprox.transcriber.properties.Property

public interface MessageFormatter {
    public fun format(
        clientPacket: Boolean,
        name: String,
        properties: List<Property>,
        indentation: Int,
    ): String

    /**
     * Formats an interface:componentId combination.
     * If the component id is -1, only an interfaceId is provided.
     */
    public fun com(
        interfaceId: Int,
        componentId: Int,
    ): String

    public fun script(id: Int): String

    public fun coord(
        level: Int,
        x: Int,
        z: Int,
    ): String

    public fun type(
        type: ScriptVarType,
        id: Int,
    ): String

    public fun zoneCoord(
        level: Int,
        zoneX: Int,
        zoneZ: Int,
    ): String

    public fun varp(id: Int): String

    public fun varbit(id: Int): String
}
