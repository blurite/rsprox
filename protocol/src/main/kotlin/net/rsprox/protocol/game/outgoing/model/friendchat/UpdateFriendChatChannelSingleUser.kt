package net.rsprox.protocol.game.outgoing.model.friendchat

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update friendchat singleuser is used to perform a change
 * to a friend chat for a single user, whether that be
 * adding the user to the friend chat, or removing them.
 * @property user the user entry being removed or added.
 * Use [AddedFriendChatUser] and [RemovedFriendChatUser]
 * respectively to perform different updates.
 */
public class UpdateFriendChatChannelSingleUser(
    public val user: FriendChatUser,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateFriendChatChannelSingleUser

        return user == other.user
    }

    override fun hashCode(): Int {
        return user.hashCode()
    }

    override fun toString(): String {
        return "UpdateFriendChatChannelSingleUser(user=$user)"
    }

    public sealed interface FriendChatUser {
        public val name: String
        public val worldId: Int
        public val rank: Int
    }

    /**
     * Added friendchat user indicates a single player
     * that is being added to the given friend chat channel.
     * @property name the name of the player being added to the friend chat
     * @property worldId the id of the world in which that player resides
     * @property rank the rank of that player in the friend chat
     * @property worldName world name, unused in OldSchool RuneScape.
     */
    public class AddedFriendChatUser private constructor(
        override val name: String,
        private val _worldId: UShort,
        private val _rank: Byte,
        public val worldName: String,
    ) : FriendChatUser {
        public constructor(
            name: String,
            worldId: Int,
            rank: Int,
            string: String,
        ) : this(
            name,
            worldId.toUShort(),
            rank.toByte(),
            string,
        ) {
            require(rank != -128) {
                "Rank cannot be -128 as that is used to indicate a removed entry."
            }
        }

        override val worldId: Int
            get() = _worldId.toInt()
        override val rank: Int
            get() = _rank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AddedFriendChatUser

            if (name != other.name) return false
            if (_worldId != other._worldId) return false
            if (_rank != other._rank) return false
            if (worldName != other.worldName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + _worldId.hashCode()
            result = 31 * result + _rank
            result = 31 * result + worldName.hashCode()
            return result
        }

        override fun toString(): String {
            return "AddedFriendChatUser(" +
                "name='$name', " +
                "worldId=$worldId, " +
                "rank=$rank, " +
                "worldName='$worldName'" +
                ")"
        }
    }

    /**
     * Removed friendchat user indicates that a player
     * is leaving a friend chat channel.
     * @property name the name of the player leaving this friend chat channel
     * @property worldId the id of the world in which the player resided.
     * Note that the world id must match up or the user will not be removed.
     */
    public class RemovedFriendChatUser private constructor(
        override val name: String,
        private val _worldId: UShort,
    ) : FriendChatUser {
        public constructor(
            name: String,
            worldId: Int,
        ) : this(
            name,
            worldId.toUShort(),
        )

        override val worldId: Int
            get() = _worldId.toInt()
        override val rank: Int
            get() = -128

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RemovedFriendChatUser

            if (name != other.name) return false
            if (_worldId != other._worldId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + _worldId.hashCode()
            return result
        }

        override fun toString(): String {
            return "RemovedFriendChatUser(" +
                "name='$name', " +
                "worldId=$worldId" +
                ")"
        }
    }
}
