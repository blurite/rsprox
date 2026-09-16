package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.coordGridProperty
import net.rsprox.shared.property.decimalCoordGridProperty
import net.rsprox.shared.property.regular.DecimalCoordGridProperty
import net.rsprox.shared.property.regular.ScriptVarTypeProperty
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore

/** Applies the presentation setting at write time; tracked coordinates always stay in instance space. */
internal class Rs3CoordinateProperties(
    private val state: Rs3SessionState,
    private val settingSetStore: SettingSetStore,
) {
    fun translate(coord: CoordGrid): CoordGrid =
        if (settingSetStore.getActive()[Setting.TRANSLATE_INSTANCED_COORDS]) {
            state.getActiveWorld().instanceCoord(coord) ?: coord
        } else {
            coord
        }

    fun append(
        property: Property,
        name: String,
        coord: CoordGrid,
    ): ScriptVarTypeProperty<*> = property.coordGrid(name, translate(coord))

    fun append(
        property: Property,
        level: Int,
        x: Int,
        z: Int,
        name: String = "coord",
    ): ScriptVarTypeProperty<*> {
        if (level !in 0..3 || x !in 0..16383 || z !in 0..16383) {
            return property.coordGridProperty(level, x, z, name)
        }
        return append(property, name, CoordGrid(level, x, z))
    }

    fun appendFine(
        property: Property,
        level: Int,
        x: Int,
        z: Int,
        fineX: Int,
        fineZ: Int,
        name: String,
    ): DecimalCoordGridProperty {
        // Shared transcript fractions use 128 units per tile, not native RS3's 512.
        val mapped =
            if (settingSetStore.getActive()[Setting.TRANSLATE_INSTANCED_COORDS]) {
                state.getActiveWorld().instanceFineCoord(level, x * 128 + fineX, z * 128 + fineZ, 128)
            } else {
                null
            }
        return if (mapped == null) {
            property.decimalCoordGridProperty(level, x, z, fineX, fineZ, name)
        } else {
            property.decimalCoordGridProperty(
                mapped.level,
                mapped.x / 128,
                mapped.z / 128,
                mapped.x % 128,
                mapped.z % 128,
                name,
            )
        }
    }
}
