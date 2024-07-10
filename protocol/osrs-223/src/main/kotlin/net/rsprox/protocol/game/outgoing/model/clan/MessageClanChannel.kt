package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Message clan channel is used to send messages within a clan channel
 * that the player is in.
 * @property clanType the type of the clan the player is in
 * @property name the name of the player sending the message
 * @property worldId the id of the world from which the message is sent
 * @property worldMessageCounter the world-local message counter.
 * Each world must have its own message counter which is used to create
 * a unique id for each message. This message counter must be
 * incrementing with each message that is sent out.
 * If two messages share the same unique id (which is a combination of
 * the [worldId] and the [worldMessageCounter] properties),
 * the client will not render the second message if it already has one
 * received in the last 100 messages.
 * It is additionally worth noting that servers with low population
 * should probably not start the counter at the same value with each
 * game boot, as the probability of multiple messages coinciding
 * is relatively high in that scenario, given the low quantity of
 * messages sent out to begin with.
 * Additionally, only the first 24 bits of the counter are utilized,
 * meaning a value from 0 to 16,777,215 (inclusive).
 * A good starting point for message counting would be to take the
 * hour of the year and multiply it by 50,000 when the server boots
 * up. This means the roll-over happens roughly after every two weeks.
 * Fine-tuning may be used to make it more granular, but the overall
 * idea remains the same.
 * @property chatCrownType the chat crown type to be rendered next to the name
 * @property message the message to send
 */
public class MessageClanChannel private constructor(
    private val _clanType: Byte,
    public val name: String,
    private val _worldId: UShort,
    public val worldMessageCounter: Int,
    private val _chatCrownType: UByte,
    public val message: String,
) : OutgoingGameMessage {
    public constructor(
        clanType: Int,
        name: String,
        worldId: Int,
        worldMessageCounter: Int,
        chatCrownType: Int,
        message: String,
    ) : this(
        clanType.toByte(),
        name,
        worldId.toUShort(),
        worldMessageCounter,
        chatCrownType.toUByte(),
        message,
    )

    public val clanType: Int
        get() = _clanType.toInt()
    public val worldId: Int
        get() = _worldId.toInt()
    public val chatCrownType: Int
        get() = _chatCrownType.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageClanChannel

        if (_clanType != other._clanType) return false
        if (name != other.name) return false
        if (_worldId != other._worldId) return false
        if (worldMessageCounter != other.worldMessageCounter) return false
        if (_chatCrownType != other._chatCrownType) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _clanType.toInt()
        result = 31 * result + name.hashCode()
        result = 31 * result + _worldId.hashCode()
        result = 31 * result + worldMessageCounter
        result = 31 * result + _chatCrownType.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageClanChannel(" +
            "clanType=$clanType, " +
            "name='$name', " +
            "worldId=$worldId, " +
            "worldLocalCounter=$worldMessageCounter, " +
            "chatCrownType=$chatCrownType, " +
            "message='$message'" +
            ")"
    }
}
