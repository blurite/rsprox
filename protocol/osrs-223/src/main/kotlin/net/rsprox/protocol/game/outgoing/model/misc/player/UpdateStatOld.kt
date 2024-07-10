package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update stat old packet is used to set the current experience
 * and levels of a skill for a given player, excluding the new
 * invisible boosted level property.
 * @property stat the id of the stat to update
 * @property currentLevel player's current level in that stat,
 * e.g. boosted or drained.
 * @property experience player's experience in the skill,
 * in its integer form - expected value range 0 to 200,000,000.
 */
public class UpdateStatOld private constructor(
    private val _stat: UByte,
    private val _currentLevel: UByte,
    public val experience: Int,
) : IncomingServerGameMessage {
    public constructor(
        stat: Int,
        currentLevel: Int,
        experience: Int,
    ) : this(
        stat.toUByte(),
        currentLevel.toUByte(),
        experience,
    )

    public val stat: Int
        get() = _stat.toInt()
    public val currentLevel: Int
        get() = _currentLevel.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateStatOld

        if (_stat != other._stat) return false
        if (_currentLevel != other._currentLevel) return false
        if (experience != other.experience) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _stat.hashCode()
        result = 31 * result + _currentLevel.hashCode()
        result = 31 * result + experience
        return result
    }

    override fun toString(): String {
        return "UpdateStat(" +
            "stat=$stat, " +
            "currentLevel=$currentLevel, " +
            "experience=$experience" +
            ")"
    }
}
