package net.rsprox.protocol.rs3.game.outgoing.model.appearance

import net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo.extendedinfo.PlayerExtendedInfo

/** Shared equipment/customisation body; profile names and levels are not part of this structure. */
public data class AppearanceBody(
    public val npc: Int?,
    public val npcTeam: Int?,
    public val equipment: List<PlayerExtendedInfo.Equipment>,
    public val customisations: List<PlayerExtendedInfo.Customisation>,
    public val primaryColours: List<Int>,
    public val secondaryColours: List<Int>,
    public val renderAnimationSet: Int,
)
