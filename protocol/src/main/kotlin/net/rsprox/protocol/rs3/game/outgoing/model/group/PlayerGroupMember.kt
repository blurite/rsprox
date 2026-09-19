package net.rsprox.protocol.rs3.game.outgoing.model.group

import net.rsprox.protocol.rs3.common.TypedVariable

/** Wire values before native XP clamping and member-state application. */
public data class PlayerGroupMember(
    public val name: String?,
    public val flags: Int,
    public val experience: List<Int>,
    public val variables: List<TypedVariable>,
    public val world: Int,
    public val memberByte35: Int,
    public val memberState: Int,
    public val memberByte3c: Int,
)
