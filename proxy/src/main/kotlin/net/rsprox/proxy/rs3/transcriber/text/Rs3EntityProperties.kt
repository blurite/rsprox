package net.rsprox.proxy.rs3.transcriber.text

import net.rsprox.protocol.common.CoordGrid
import net.rsprox.proxy.rs3.transcriber.state.Rs3SessionState
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.any
import net.rsprox.shared.property.identifiedNpc
import net.rsprox.shared.property.identifiedPlayer
import net.rsprox.shared.property.int
import net.rsprox.shared.property.shortNpc
import net.rsprox.shared.property.shortPlayer
import net.rsprox.shared.property.unidentifiedNpc
import net.rsprox.shared.property.unidentifiedPlayer
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingSetStore

internal class Rs3EntityProperties(
    private val state: Rs3SessionState,
    private val settingSetStore: SettingSetStore,
) {
    private val coordinates = Rs3CoordinateProperties(state, settingSetStore)

    fun player(property: Property, index: Int): ChildProperty<*> {
        val player = state.getPlayerOrNull(index) ?: return property.unidentifiedPlayer(index)
        if (player.name == null || player.level == null || player.x == null || player.z == null) {
            return property.shortPlayer(index, player.name)
        }
        val visibleIndex = if (settingSetStore.getActive()[Setting.PLAYER_HIDE_INDEX]) Int.MIN_VALUE else index
        val coord = coordinates.translate(CoordGrid(player.level, player.x, player.z))
        return property.identifiedPlayer(visibleIndex, player.name, coord.level, coord.x, coord.z)
    }

    fun npc(property: Property, index: Int): ChildProperty<*> {
        val npc = state.getActiveWorld().getNpcOrNull(index) ?: return property.unidentifiedNpc(index)
        val position = npc.coord ?: return property.shortNpc(index, npc.id)
        val coord = coordinates.translate(position)
        val visibleIndex = if (settingSetStore.getActive()[Setting.HIDE_NPC_INDICES]) Int.MIN_VALUE else index
        return property.identifiedNpc(
            visibleIndex, npc.id, npc.name ?: "null", coord.level, coord.x, coord.z, npc.spawnAngle,
        )
    }

    /** Packed actor identity shared by FACE_ENTITY and SET_TARGET: type byte followed by a 16-bit index. */
    fun entity(property: Property, target: Int) {
        val kind = target ushr 16
        val index = target and 0xFFFF
        // Native 950 masks dispatch by the high byte; this is not a threshold-based actor index.
        when (kind) {
            1 -> npc(property, index)
            2 -> player(property, index)
            127, 255 -> property.any<Any>("entity", null)
            else -> {
                // Preserve unknown tags explicitly rather than guessing an actor type.
                property.any("entitytype", "unknown")
                property.int("kind", kind)
                property.int("index", index)
            }
        }
    }
}
