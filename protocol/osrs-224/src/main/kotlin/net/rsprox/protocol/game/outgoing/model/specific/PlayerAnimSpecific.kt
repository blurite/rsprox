package net.rsprox.protocol.game.outgoing.model.specific

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Player anim specifics are used to play an animation on the local player for the local player,
 * not the entire world.
 * Note that unlike most other packets, this one does not provide the index, so it can only
 * be played on the local player and no one else.
 * @property id the id of the animation
 * @property delay the delay of the animation before it begins playing in client cycles (20ms/cc)
 */
public class PlayerAnimSpecific private constructor(
    private val _id: UShort,
    private val _delay: UByte,
) : IncomingServerGameMessage {
    public constructor(
        id: Int,
        delay: Int,
    ) : this(
        id.toUShort(),
        delay.toUByte(),
    )

    public val id: Int
        get() = _id.toInt()
    public val delay: Int
        get() = _delay.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PlayerAnimSpecific

        if (_id != other._id) return false
        if (_delay != other._delay) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + _delay.hashCode()
        return result
    }

    override fun toString(): String {
        return "PlayerAnimSpecific(" +
            "id=$id, " +
            "delay=$delay" +
            ")"
    }
}
