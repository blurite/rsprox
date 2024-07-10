package net.rsprox.protocol.game.outgoing.model.friendchat

public sealed class UpdateFriendChatChannelFull {
    public abstract val channelOwner: String
    public abstract val channelName: String
    public abstract val kickRank: Int
    public abstract val entries: List<FriendChatEntry>

    /**
     * A class to contain all the properties of a player in a friend chat.
     * @property name the name of the player that is in the friend chat
     * @property worldId the id of the world in which the given user is
     * @property rank the rank of the given used in this friend chat
     * @property worldName world name, unused in OldSchool RuneScape.
     */
    public class FriendChatEntry private constructor(
        public val name: String,
        private val _worldId: UShort,
        private val _rank: Byte,
        public val worldName: String,
    ) {
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
        )

        public val worldId: Int
            get() = _worldId.toInt()
        public val rank: Int
            get() = _rank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FriendChatEntry

            if (name != other.name) return false
            if (_worldId != other._worldId) return false
            if (_rank != other._rank) return false
            if (worldName != other.worldName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + _worldId.hashCode()
            result = 31 * result + _rank.hashCode()
            result = 31 * result + worldName.hashCode()
            return result
        }

        override fun toString(): String {
            return "FriendChatEntry(" +
                "name='$name', " +
                "worldId=$worldId, " +
                "rank=$rank, " +
                "worldName='$worldName'" +
                ")"
        }
    }
}
