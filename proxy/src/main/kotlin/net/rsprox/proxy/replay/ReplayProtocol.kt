package net.rsprox.proxy.replay

import net.rsprot.protocol.Prot

internal fun Prot.isReplayServerTickEnd(): Boolean {
    return hasReplayName("SERVER_TICK_END")
}

internal fun Prot.isReplayReconnect(): Boolean {
    return hasReplayName("RECONNECT")
}

internal fun Prot.isReplayRebuildNormal(): Boolean {
    return hasAnyReplayName("REBUILD_NORMAL", "REBUILD_NORMAL_V1", "REBUILD_NORMAL_V2")
}

internal fun Prot.isReplayMapRebuild(): Boolean {
    return hasAnyReplayName(
        "REBUILD_NORMAL",
        "REBUILD_REGION",
        "REBUILD_NORMAL_V1",
        "REBUILD_NORMAL_V2",
        "REBUILD_REGION_V1",
        "REBUILD_REGION_V2",
    )
}

internal fun Prot.isActiveWorldV1(): Boolean {
    return hasReplayName("SET_ACTIVE_WORLD_V1")
}

internal fun Prot.isActiveWorldV2(): Boolean {
    return hasReplayName("SET_ACTIVE_WORLD_V2")
}

internal fun Prot.isReplayPacketGroupMarker(): Boolean {
    return hasAnyReplayName("PACKET_GROUP_START", "PACKET_GROUP_END")
}

internal fun Prot.isReplayPacketGroupStart(): Boolean {
    return hasReplayName("PACKET_GROUP_START")
}

internal fun Prot.isReplayMapBuildComplete(): Boolean {
    return hasReplayName("MAP_BUILD_COMPLETE")
}

internal fun Prot.isReplaySendPing(): Boolean {
    return hasReplayName("SEND_PING")
}

internal fun Prot.isReplaySynthSound(): Boolean {
    return hasReplayName("SYNTH_SOUND")
}

internal fun Prot.isReplayIncomingZoneUpdate(): Boolean {
    val name = toString()
    return name.equals("UPDATE_ZONE_FULL_FOLLOWS", ignoreCase = true) ||
        name.equals("UPDATE_ZONE_PARTIAL_FOLLOWS", ignoreCase = true) ||
        name.equals("UPDATE_ZONE_PARTIAL_ENCLOSED", ignoreCase = true) ||
        name.startsWith("LOC_", ignoreCase = true) ||
        name.startsWith("OBJ_", ignoreCase = true) ||
        name.startsWith("MAP_ANIM", ignoreCase = true) ||
        name.startsWith("MAP_PROJANIM", ignoreCase = true) ||
        name.startsWith("SCRIPTEDPROJ_", ignoreCase = true) ||
        name.equals("SOUND_AREA", ignoreCase = true)
}

internal fun ReplayFrame.replayWireSize(): Int {
    val opcodeSize = if (prot.opcode >= 0x80) 2 else 1
    val sizePrefix =
        when (prot.size) {
            Prot.VAR_BYTE -> 1
            Prot.VAR_SHORT -> 2
            else -> 0
        }
    return opcodeSize + sizePrefix + payload.size
}

private fun Prot.hasReplayName(name: String): Boolean {
    return toString().equals(name, ignoreCase = true)
}

private fun Prot.hasAnyReplayName(vararg names: String): Boolean {
    return names.any { hasReplayName(it) }
}
