package net.rsprox.transcriber.rs3.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.com
import net.rsprox.shared.property.coordGridProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.property.scriptVarType

internal fun Property.component(
    name: String,
    packed: Int,
): ScriptVarTypeProperty<*> {
    return com(name, packed ushr 16, packed and 0xFFFF)
}

internal fun Property.component(
    name: String,
    packed: Long,
): ScriptVarTypeProperty<*> {
    return component(name, packed.toInt())
}

internal fun Property.coordGrid(
    name: String,
    coord: CoordGrid,
): ScriptVarTypeProperty<*> {
    return scriptVarType(name, ScriptVarType.COORDGRID, coord.packed)
}

internal fun Property.coordGrid(
    level: Int,
    x: Int,
    z: Int,
    name: String = "coord",
): ScriptVarTypeProperty<*> {
    return coordGridProperty(level, x, z, name)
}
