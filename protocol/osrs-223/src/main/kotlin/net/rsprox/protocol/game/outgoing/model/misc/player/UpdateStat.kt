package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Update stat packet is used to set the current experience
 * and levels of a skill for a given player.
 * @property stat the id of the stat to update
 * @property currentLevel player's current level in that stat,
 * e.g. boosted or drained.
 * @property invisibleBoostedLevel player's level in the stat
 * with invisible boosts included
 * @property experience player's experience in the skill,
 * in its integer form - expected value range 0 to 200,000,000.
 */
public class UpdateStat private constructor(
    private val _stat: UByte,
    private val _currentLevel: UByte,
    private val _invisibleBoostedLevel: UByte,
    public val experience: Int,
) : OutgoingGameMessage {
    public constructor(
        stat: Int,
        currentLevel: Int,
        invisibleBoostedLevel: Int,
        experience: Int,
    ) : this(
        stat.toUByte(),
        currentLevel.toUByte(),
        invisibleBoostedLevel.toUByte(),
        experience,
    )

    public val stat: Int
        get() = _stat.toInt()
    public val currentLevel: Int
        get() = _currentLevel.toInt()
    public val invisibleBoostedLevel: Int
        get() = _invisibleBoostedLevel.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateStat

        if (_stat != other._stat) return false
        if (_currentLevel != other._currentLevel) return false
        if (_invisibleBoostedLevel != other._invisibleBoostedLevel) return false
        if (experience != other.experience) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _stat.hashCode()
        result = 31 * result + _currentLevel.hashCode()
        result = 31 * result + _invisibleBoostedLevel.hashCode()
        result = 31 * result + experience
        return result
    }

    override fun toString(): String {
        return "UpdateStat(" +
            "stat=$stat, " +
            "currentLevel=$currentLevel, " +
            "invisibleBoostedLevel=$invisibleBoostedLevel, " +
            "experience=$experience" +
            ")"
    }
}
