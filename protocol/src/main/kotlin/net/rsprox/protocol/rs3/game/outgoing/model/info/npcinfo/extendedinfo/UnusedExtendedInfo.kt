package net.rsprox.protocol.rs3.game.outgoing.model.info.npcinfo.extendedinfo

/** Parsed native-discarded fields without an established cross-revision identity. */
public data class UnusedExtendedInfo(
    public val bit: Int,
    public val fields: List<Int>,
) : NpcExtendedInfo
