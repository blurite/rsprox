package net.rsprox.protocol.game.outgoing.model.friendchat

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Message friendchannel is used to transmit messages within a friend
 * chat channel.
 * @property sender the name of the player who is sending the message
 * @property channelName the name of the friend chat channel
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
 * @property chatCrownType the id of the crown to render next to the
 * name of the sender.
 * @property message the message to be sent in the friend chat
 * channel.
 */
public class MessageFriendChannel private constructor(
    public val sender: String,
    public val channelName: String,
    private val _worldId: UShort,
    public val worldMessageCounter: Int,
    private val _chatCrownType: UByte,
    public val message: String,
) : IncomingServerGameMessage {
    public constructor(
        sender: String,
        channelName: String,
        worldId: Int,
        worldMessageCounter: Int,
        chatCrownType: Int,
        message: String,
    ) : this(
        sender,
        channelName,
        worldId.toUShort(),
        worldMessageCounter,
        chatCrownType.toUByte(),
        message,
    )

    public val worldId: Int
        get() = _worldId.toInt()
    public val chatCrownType: Int
        get() = _chatCrownType.toInt()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MessageFriendChannel

        if (sender != other.sender) return false
        if (channelName != other.channelName) return false
        if (_worldId != other._worldId) return false
        if (worldMessageCounter != other.worldMessageCounter) return false
        if (_chatCrownType != other._chatCrownType) return false
        if (message != other.message) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sender.hashCode()
        result = 31 * result + channelName.hashCode()
        result = 31 * result + _worldId.hashCode()
        result = 31 * result + worldMessageCounter
        result = 31 * result + _chatCrownType.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }

    override fun toString(): String {
        return "MessageFriendChannel(" +
            "sender='$sender', " +
            "channelName='$channelName', " +
            "worldId=$worldId, " +
            "worldMessageCounter=$worldMessageCounter, " +
            "chatCrownType=$chatCrownType, " +
            "message='$message'" +
            ")"
    }
}
