package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Clan channel delta is a packet used to transmit partial updates
 * to an existing clan channel. This prevents sending a full update for everything
 * as that can get rather wasteful.
 * @property clanType the type of the clan the player is in
 * @property clanHash the 64-bit hash of the clan
 * @property updateNum the update counter/timestamp for the clan.
 * The exact behaviours behind this are not known, but the value appears to be
 * an epoch time millis, with each minor change resulting in the value incrementing
 * by +1; e.g. each member joining seems to increment the value by 1.
 * @property events the list of channel delta events to perform in this update
 */
public class ClanChannelDelta private constructor(
    private val _clanType: Byte,
    public val clanHash: Long,
    public val updateNum: Long,
    public val events: List<ClanChannelDeltaEvent>,
) : OutgoingGameMessage {
    public constructor(
        clanType: Int,
        key: Long,
        updateNum: Long,
        events: List<ClanChannelDeltaEvent>,
    ) : this(
        clanType.toByte(),
        key,
        updateNum,
        events,
    )

    public val clanType: Int
        get() = _clanType.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClanChannelDelta

        if (_clanType != other._clanType) return false
        if (clanHash != other.clanHash) return false
        if (updateNum != other.updateNum) return false
        if (events != other.events) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _clanType.toInt()
        result = 31 * result + clanHash.hashCode()
        result = 31 * result + updateNum.hashCode()
        result = 31 * result + events.hashCode()
        return result
    }

    override fun toString(): String {
        return "ClanChannelDelta(" +
            "clanType=$clanType, " +
            "clanHash=$clanHash, " +
            "updateNum=$updateNum, " +
            "events=$events" +
            ")"
    }

    public sealed interface ClanChannelDeltaEvent

    /**
     * Clan channel delta adduser event is used to add a new user
     * into the clan.
     * @property name the name of the player to add to the clan
     * @property world the id of the world in which the player resides
     * @property rank the rank of the player within the clan
     */
    public class ClanChannelDeltaAddUserEvent private constructor(
        public val name: String,
        private val _world: UShort,
        private val _rank: Byte,
    ) : ClanChannelDeltaEvent {
        public constructor(
            name: String,
            world: Int,
            rank: Int,
        ) : this(
            name,
            world.toUShort(),
            rank.toByte(),
        )

        public val world: Int
            get() = _world.toInt()
        public val rank: Int
            get() = _rank.toInt()
    }

    /**
     * Clan channel delta update base settings event is used to modify the base
     * settings of a clan.
     * @property clanName the clan name to set
     * @property talkRank the minimum rank needed to talk
     * @property kickRank the minimum rank needed to kick other members
     */
    public class ClanChannelDeltaUpdateBaseSettingsEvent private constructor(
        public val clanName: String?,
        private val _talkRank: Byte,
        private val _kickRank: Byte,
    ) : ClanChannelDeltaEvent {
        public constructor() : this(
            null,
            0,
            0,
        )

        public constructor(
            clanName: String,
            talkRank: Int,
            kickRank: Int,
        ) : this(
            clanName,
            talkRank.toByte(),
            kickRank.toByte(),
        )

        public val talkRank: Int
            get() = _talkRank.toInt()
        public val kickRank: Int
            get() = _kickRank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanChannelDeltaUpdateBaseSettingsEvent

            if (clanName != other.clanName) return false
            if (_talkRank != other._talkRank) return false
            if (_kickRank != other._kickRank) return false

            return true
        }

        override fun hashCode(): Int {
            var result = clanName?.hashCode() ?: 0
            result = 31 * result + _talkRank
            result = 31 * result + _kickRank
            return result
        }

        override fun toString(): String {
            return "ClanChannelDeltaUpdateBaseSettingsEvent(" +
                "clanName=$clanName, " +
                "talkRank=$talkRank, " +
                "kickRank=$kickRank" +
                ")"
        }
    }

    /**
     * Clan channel delta delete user event is used to delete an existing
     * member from the clan.
     * @property index the index of the player within the clan.
     * Note that this index is the index within this clan, and not a global
     * index of the player.
     */
    public class ClanChannelDeltaDeleteUserEvent private constructor(
        private val _index: UShort,
    ) : ClanChannelDeltaEvent {
        public constructor(
            index: Int,
        ) : this(
            index.toUShort(),
        )

        public val index: Int
            get() = _index.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanChannelDeltaDeleteUserEvent

            return _index == other._index
        }

        override fun hashCode(): Int {
            return _index.hashCode()
        }

        override fun toString(): String {
            return "ClanChannelDeltaDeleteUserEvent(index=$index)"
        }
    }

    /**
     * Clan channel delta update user details event is used to modify
     * the details of a user in the clan.
     * @property index the index of the player whom to update within the clan.
     * Note that this is the index within the clan's list of members and not
     * the world-global indexed player list.
     * @property name the new name of this player within the clan
     * @property rank the new rank of this player within the clan
     * @property world the new world of this player within the clan
     */
    public class ClanChannelDeltaUpdateUserDetailsEvent private constructor(
        private val _index: UShort,
        public val name: String,
        private val _rank: Byte,
        private val _world: UShort,
    ) : ClanChannelDeltaEvent {
        public constructor(
            index: Int,
            name: String,
            rank: Int,
            world: Int,
        ) : this(
            index.toUShort(),
            name,
            rank.toByte(),
            world.toUShort(),
        )

        public val index: Int
            get() = _index.toInt()
        public val rank: Int
            get() = _rank.toInt()
        public val world: Int
            get() = _world.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanChannelDeltaUpdateUserDetailsEvent

            if (_index != other._index) return false
            if (name != other.name) return false
            if (_rank != other._rank) return false
            if (_world != other._world) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + _index.hashCode()
            result = 31 * result + _rank
            result = 31 * result + _world.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanChannelDeltaUpdateUserDetailsEvent(" +
                "index=$index, " +
                "name='$name', " +
                "rank=$rank, " +
                "world=$world" +
                ")"
        }
    }

    /**
     * Clan channel delta update user details v2 event is used to modify
     * the details of a user in the clan.
     * Note that this class is identical to the [ClanChannelDeltaUpdateUserDetailsEvent],
     * with the only exception being that more bandwidth is used to transmit this update,
     * as there are multiple unused properties being sent on-top.
     * @property index the index of the player whom to update within the clan.
     * Note that this is the index within the clan's list of members and not
     * the world-global indexed player list.
     * @property name the new name of this player within the clan
     * @property rank the new rank of this player within the clan
     * @property world the new world of this player within the clan
     */
    public class ClanChannelDeltaUpdateUserDetailsV2Event private constructor(
        private val _index: UShort,
        public val name: String,
        private val _rank: Byte,
        private val _world: UShort,
    ) : ClanChannelDeltaEvent {
        public constructor(
            index: Int,
            name: String,
            rank: Int,
            world: Int,
        ) : this(
            index.toUShort(),
            name,
            rank.toByte(),
            world.toUShort(),
        )

        public val index: Int
            get() = _index.toInt()
        public val rank: Int
            get() = _rank.toInt()
        public val world: Int
            get() = _world.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanChannelDeltaUpdateUserDetailsV2Event

            if (_index != other._index) return false
            if (name != other.name) return false
            if (_rank != other._rank) return false
            if (_world != other._world) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + _index.hashCode()
            result = 31 * result + _rank
            result = 31 * result + _world.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanChannelDeltaUpdateUserDetailsV2Event(" +
                "index=$index, " +
                "name='$name', " +
                "rank=$rank, " +
                "world=$world" +
                ")"
        }
    }
}
