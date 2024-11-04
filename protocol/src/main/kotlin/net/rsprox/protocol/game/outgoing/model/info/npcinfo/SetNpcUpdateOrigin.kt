package net.rsprox.protocol.game.outgoing.model.info.npcinfo

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * The set npc update origin packet is used to set the relative coordinate for npc info packet.
 * As of revision 222, with the introduction of world entities, it is no longer viable to solely
 * rely on the local player's coordinate, as it may be impacted by a specific world entity.
 * As such, npc info updates should now be prefaced with the origin update to mark the relative coord.
 * For no-world-entity use cases, just pass the player's coordinate in the current build area to
 * get the old behavior.
 *
 * @property originX the x coordinate within the current build area of the player relative
 * to which NPCs will be placed within NPC info packet.
 * @property originZ the z coordinate within the current build area of the player relative
 * to which NPCs will be placed within NPC info packet.
 */
public class SetNpcUpdateOrigin private constructor(
    private val _originX: UByte,
    private val _originZ: UByte,
) : IncomingServerGameMessage {
    public constructor(
        originX: Int,
        originZ: Int,
    ) : this(
        originX.toUByte(),
        originZ.toUByte(),
    )

    public val originX: Int
        get() = _originX.toInt()
    public val originZ: Int
        get() = _originZ.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SetNpcUpdateOrigin

        if (_originX != other._originX) return false
        if (_originZ != other._originZ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _originX.hashCode()
        result = 31 * result + _originZ.hashCode()
        return result
    }

    override fun toString(): String {
        return "SetNpcUpdateOrigin(" +
            "originX=$originX, " +
            "originZ=$originZ" +
            ")"
    }
}
