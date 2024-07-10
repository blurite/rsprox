package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Clan channel full packets are used to update
 * the state of a clan upon first joining it, or when the player is leaving it.
 * @property clanType the type of the clan the player is joining or leaving,
 * such as guest or normal.
 * @property update the type of update to perform, either [ClanChannelFullJoinUpdate]
 * or [ClanChannelFullLeaveUpdate].
 */
public class ClanChannelFull private constructor(
    private val _clanType: Byte,
    public val update: ClanChannelFullUpdate,
) : IncomingServerGameMessage {
    public constructor(
        clanType: Int,
        update: ClanChannelFullUpdate,
    ) : this(
        clanType.toByte(),
        update,
    )

    public val clanType: Int
        get() = _clanType.toInt()

    public sealed interface ClanChannelFullUpdate

    /**
     * Clan channel full join update implies the user is joining
     * a new clan.
     * @property useBase37Names whether to send the names of players
     * in a base-37 encoding. In OldSchool RuneScape, this option is unused.
     * @property useDisplayNames whether to use display names for encoding.
     * In OldSchool RuneScape, this is always the case and cannot be opted out of.
     * @property hasVersion whether a custom version id is provided.
     * It is unclear what the purpose behind this is, as the values are discarded.
     * @property version the version id, defaulting to 2 in OldSchool RuneScape.
     * @property clanHash the 64-bit hash of the clan
     * @property updateNum the update counter/timestamp for the clan.
     * The exact behaviours behind this are not known, but the value appears to be
     * an epoch time millis, with each minor change resulting in the value incrementing
     * by +1; e.g. each member joining seems to increment the value by 1.
     * @property clanName the name of the clan
     * @property discardedBoolean currently unknown as the client discards this value
     * @property kickRank the minimum rank needed to kick other players from the clan
     * @property talkRank the minimum rank needed to talk in the clan
     * @property members the list of members within this clan.
     */
    public class ClanChannelFullJoinUpdate private constructor(
        private val _flags: UByte,
        private val _version: UByte,
        public val clanHash: Long,
        public val updateNum: Long,
        public val clanName: String,
        public val discardedBoolean: Boolean,
        private val _kickRank: Byte,
        private val _talkRank: Byte,
        public val members: List<ClanMember>,
    ) : ClanChannelFullUpdate {
        public constructor(
            key: Long,
            updateNum: Long,
            clanName: String,
            discardedBoolean: Boolean,
            kickRank: Int,
            talkRank: Int,
            members: List<ClanMember>,
            version: Int = DEFAULT_OLDSCHOOL_VERSION,
            base37Names: Boolean = false,
        ) : this(
            (
                FLAG_USE_DISPLAY_NAMES
                    .or(if (base37Names) FLAG_USE_BASE_37_NAMES else 0)
                    .or(if (version != DEFAULT_OLDSCHOOL_VERSION) FLAG_HAS_VERSION else 0)
            ).toUByte(),
            version.toUByte(),
            key,
            updateNum,
            clanName,
            discardedBoolean,
            kickRank.toByte(),
            talkRank.toByte(),
            members,
        )

        public val useBase37Names: Boolean
            get() = _flags.toInt() and FLAG_USE_BASE_37_NAMES != 0
        public val useDisplayNames: Boolean
            get() = _flags.toInt() and FLAG_USE_DISPLAY_NAMES != 0
        public val hasVersion: Boolean
            get() = _flags.toInt() and FLAG_HAS_VERSION != 0
        public val version: Int
            get() = _version.toInt()
        public val flags: Int
            get() = _flags.toInt()
        public val kickRank: Int
            get() = _kickRank.toInt()
        public val talkRank: Int
            get() = _talkRank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanChannelFullJoinUpdate

            if (_flags != other._flags) return false
            if (_version != other._version) return false
            if (clanHash != other.clanHash) return false
            if (updateNum != other.updateNum) return false
            if (clanName != other.clanName) return false
            if (discardedBoolean != other.discardedBoolean) return false
            if (_kickRank != other._kickRank) return false
            if (_talkRank != other._talkRank) return false
            if (members != other.members) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _flags.toInt()
            result = 31 * result + _version.hashCode()
            result = 31 * result + clanHash.hashCode()
            result = 31 * result + updateNum.hashCode()
            result = 31 * result + clanName.hashCode()
            result = 31 * result + discardedBoolean.hashCode()
            result = 31 * result + _kickRank
            result = 31 * result + _talkRank
            result = 31 * result + members.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanChannelFullJoinUpdate(" +
                "useBase37Names=$useBase37Names, " +
                "useDisplayNames=$useDisplayNames, " +
                "hasVersion=$hasVersion, " +
                "version=$version, " +
                "key=$clanHash, " +
                "updateNum=$updateNum, " +
                "clanName='$clanName', " +
                "discardedBoolean=$discardedBoolean, " +
                "kickRank=$kickRank, " +
                "talkRank=$talkRank, " +
                "members=$members" +
                ")"
        }
    }

    /**
     * Clan channel full leave update implies the user is leaving an existing
     * clan of theirs.
     */
    public data object ClanChannelFullLeaveUpdate : ClanChannelFullUpdate

    /**
     * Clan member classes are used to wrap all the properties shown in the clan
     * interface about each player in the clan.
     * @property name the display name of the clan member
     * @property rank the rank of the clan member in the clan,
     * for guest members, the rank is set to -1
     * @property world the world in which the player resides
     * @property discardedBoolean unknown boolean (not used by the client)
     */
    public class ClanMember private constructor(
        public val name: String,
        private val _rank: Byte,
        private val _world: UShort,
        public val discardedBoolean: Boolean,
    ) {
        public constructor(
            name: String,
            rank: Int,
            world: Int,
            discardedBoolean: Boolean,
        ) : this(
            name,
            rank.toByte(),
            world.toUShort(),
            discardedBoolean,
        )

        public constructor(
            name: String,
            rank: Int,
            world: Int,
        ) : this(
            name,
            rank.toByte(),
            world.toUShort(),
            false,
        )

        public val rank: Int
            get() = _rank.toInt()
        public val world: Int
            get() = _world.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanMember

            if (name != other.name) return false
            if (_rank != other._rank) return false
            if (_world != other._world) return false
            if (discardedBoolean != other.discardedBoolean) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + _rank
            result = 31 * result + _world.hashCode()
            result = 31 * result + discardedBoolean.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanMember(" +
                "name='$name', " +
                "rank=$rank, " +
                "world=$world, " +
                "discardedBoolean=$discardedBoolean" +
                ")"
        }
    }

    public companion object {
        public const val FLAG_USE_BASE_37_NAMES: Int = 0x1
        public const val FLAG_USE_DISPLAY_NAMES: Int = 0x2
        public const val FLAG_HAS_VERSION: Int = 0x4
        public const val DEFAULT_OLDSCHOOL_VERSION: Int = 2
    }
}
