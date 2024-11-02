package net.rsprox.protocol.game.outgoing.model.social

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Update friendlist is used to send the initial friend list on login,
 * as well as any additions to the friend list over time.
 * @property friends the list of friends to be added/set to this friend list.
 * For instances of this class, use [OnlineFriend] and [OfflineFriend]
 * respectively.
 */
@Suppress("MemberVisibilityCanBePrivate")
public class UpdateFriendList(
    public val friends: List<Friend>,
) : IncomingServerGameMessage {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateFriendList

        return friends == other.friends
    }

    override fun hashCode(): Int {
        return friends.hashCode()
    }

    override fun toString(): String {
        return "UpdateFriendList(friends=$friends)"
    }

    public sealed interface Friend {
        public val added: Boolean
        public val name: String
        public val previousName: String?
        public val worldId: Int
        public val rank: Int
        public val properties: Int
        public val notes: String
    }

    /**
     * Online friends are friends who are currently logged into the game.
     * @property added whether the friend was just added to the friend list,
     * or if it's an initial load. For initial loads, the client skips existing
     * friend checks.
     * @property name the display name of the friend
     * @property previousName the previous display name of the friend,
     * if they had one. If not, set it to null.
     * @property worldId the world that the friend is logged into
     * @property rank the friend's current rank, used to determine the chat icon
     * @property properties a set of bitpacked properties; currently, the client
     * only checks for two properties - [PROPERTY_REFERRED] and [PROPERTY_REFERRER].
     * These properties only affect the ordering of friends in the player's friend list.
     * @property notes the notes on that friend. None of the clients use this value.
     * @property worldName the name of the world the player is logged into,
     * e.g. "Old School 35" for world 335 in OldSchool RuneScape.
     * @property platform the id of the client the friend is logged into.
     * Current known values include 0 for RuneScape 3, 4 for RS3's lobby (presumably),
     * and 8 for OldSchool RuneScape. The OldSchool clients do not utilize this,
     * its purpose is to prevent sending quick-chat messages from RuneScape 3 over
     * to OldSchool RuneScape, as it does not support quick chat functionality.
     * @property worldFlags the flags of the world the friend is logged into.
     */
    public class OnlineFriend private constructor(
        override val added: Boolean,
        override val name: String,
        override val previousName: String?,
        private val _worldId: UShort,
        private val _rank: UByte,
        private val _properties: UByte,
        override val notes: String,
        public val worldName: String,
        private val _platform: UByte,
        public val worldFlags: Int,
    ) : Friend {
        public constructor(
            added: Boolean,
            name: String,
            previousName: String?,
            worldId: Int,
            rank: Int,
            properties: Int,
            notes: String,
            worldName: String,
            platform: Int,
            worldFlags: Int,
        ) : this(
            added,
            name,
            previousName,
            worldId.toUShort(),
            rank.toUByte(),
            properties.toUByte(),
            notes,
            worldName,
            platform.toUByte(),
            worldFlags,
        ) {
            require(worldId > 0) {
                "World id must be greater than 0 for online friends"
            }
        }

        override val worldId: Int
            get() = _worldId.toInt()
        override val rank: Int
            get() = _rank.toInt()
        override val properties: Int
            get() = _properties.toInt()
        public val platform: Int
            get() = _platform.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OnlineFriend

            if (added != other.added) return false
            if (name != other.name) return false
            if (previousName != other.previousName) return false
            if (_worldId != other._worldId) return false
            if (_rank != other._rank) return false
            if (_properties != other._properties) return false
            if (notes != other.notes) return false
            if (worldName != other.worldName) return false
            if (_platform != other._platform) return false
            if (worldFlags != other.worldFlags) return false

            return true
        }

        override fun hashCode(): Int {
            var result = added.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + (previousName?.hashCode() ?: 0)
            result = 31 * result + _worldId.hashCode()
            result = 31 * result + _rank.hashCode()
            result = 31 * result + _properties.hashCode()
            result = 31 * result + notes.hashCode()
            result = 31 * result + worldName.hashCode()
            result = 31 * result + _platform.hashCode()
            result = 31 * result + worldFlags
            return result
        }

        override fun toString(): String {
            return "OnlineFriend(" +
                "added=$added, " +
                "name='$name', " +
                "previousName=$previousName, " +
                "worldId=$worldId, " +
                "rank=$rank, " +
                "properties=$properties, " +
                "worldName='$worldName', " +
                "platform=$platform, " +
                "worldFlags=$worldFlags, " +
                "notes='$notes'" +
                ")"
        }
    }

    /**
     * Offline friends are friends who either aren't logged in, or cannot be
     * seen as online due to preferences chosen.
     * @property added whether the friend was just added to the friend list,
     * or if it's an initial load. For initial loads, the client skips existing
     * friend checks.
     * @property name the display name of the friend
     * @property previousName the previous display name of the friend,
     * if they had one. If not, set it to null.
     * @property worldId the world that the friend is logged into
     * @property rank the friend's current rank, used to determine the chat icon
     * @property properties a set of bitpacked properties; currently, the client
     * only checks for two properties - [PROPERTY_REFERRED] and [PROPERTY_REFERRER].
     * These properties only affect the ordering of friends in the player's friend list.
     * @property notes the notes on that friend. None of the clients use this value.
     */
    public class OfflineFriend private constructor(
        override val added: Boolean,
        override val name: String,
        override val previousName: String?,
        private val _rank: UByte,
        private val _properties: UByte,
        override val notes: String,
    ) : Friend {
        public constructor(
            added: Boolean,
            name: String,
            previousName: String?,
            rank: Int,
            properties: Int,
            notes: String,
        ) : this(
            added,
            name,
            previousName,
            rank.toUByte(),
            properties.toUByte(),
            notes,
        )

        override val rank: Int
            get() = _rank.toInt()
        override val properties: Int
            get() = _properties.toInt()
        override val worldId: Int
            get() = 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OfflineFriend

            if (added != other.added) return false
            if (name != other.name) return false
            if (previousName != other.previousName) return false
            if (_rank != other._rank) return false
            if (_properties != other._properties) return false
            if (notes != other.notes) return false

            return true
        }

        override fun hashCode(): Int {
            var result = added.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + (previousName?.hashCode() ?: 0)
            result = 31 * result + _rank.hashCode()
            result = 31 * result + _properties.hashCode()
            result = 31 * result + notes.hashCode()
            return result
        }

        override fun toString(): String {
            return "OfflineFriend(" +
                "added=$added, " +
                "name='$name', " +
                "previousName=$previousName, " +
                "rank=$rank, " +
                "properties=$properties, " +
                "notes='$notes'" +
                ")"
        }
    }

    public companion object {
        /**
         * Referred property is used to assign a higher priority to a friend
         * in the friend list.
         */
        public const val PROPERTY_REFERRED: Int = 0x1

        /**
         * Referred property is used to assign a higher priority to a friend
         * in the friend list. Referrers have a higher priority than [PROPERTY_REFERRED].
         */
        public const val PROPERTY_REFERRER: Int = 0x2
    }
}
