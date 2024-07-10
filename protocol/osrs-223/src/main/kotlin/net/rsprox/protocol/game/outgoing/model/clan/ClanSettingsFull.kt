package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Clan settings full packet is used to update the clan's primary settings.
 * @property clanType the clan being updated
 * @property update the clan settings update to be performed
 */
public class ClanSettingsFull private constructor(
    private val _clanType: Byte,
    public val update: ClanSettingsFullUpdate,
) : OutgoingGameMessage {
    public constructor(
        clanType: Int,
        update: ClanSettingsFullUpdate,
    ) : this(
        clanType.toByte(),
        update,
    )

    public val clanType: Int
        get() = _clanType.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    public sealed interface ClanSettingsFullUpdate

    /**
     * Clan settings full join update is used to make the player join a clan.
     * @property updateNum the number of changes done to this clan's settings
     * @property creationTime the epoch time minute when the clan was created
     * @property clanName the name of the clan
     * @property allowUnaffined whether to allow guests to join the clan
     * @property talkRank the minimum rank needed to talk in this clan chat
     * @property kickRank the minimum rank needed to kick members from this clan
     * @property lootshareRank the minimum rank needed to toggle lootshare, unused in OldSchool.
     * @property coinshareRank the minimum rank needed to toggle coinshare, unused in OldSchool.
     * @property affinedMembers the list of affined members in this clan
     * @property bannedMembers the list of banned members in this clan
     * @property settings the list of settings to apply to this clan
     */
    public class ClanSettingsFullJoinUpdate private constructor(
        private val _flags: UByte,
        public val updateNum: Int,
        public val creationTime: Int,
        public val clanName: String,
        public val allowUnaffined: Boolean,
        private val _talkRank: Byte,
        private val _kickRank: Byte,
        private val _lootshareRank: Byte,
        private val _coinshareRank: Byte,
        public val affinedMembers: List<AffinedClanMember>,
        public val bannedMembers: List<BannedClanMember>,
        public val settings: List<ClanSetting>,
    ) : ClanSettingsFullUpdate {
        public constructor(
            updateNum: Int,
            creationTime: Int,
            clanName: String,
            allowUnaffined: Boolean,
            talkRank: Int,
            kickRank: Int,
            lootshareRank: Int,
            coinshareRank: Int,
            affinedMembers: List<AffinedClanMember>,
            bannedMembers: List<BannedClanMember>,
            settings: List<ClanSetting>,
            hasAffinedHashes: Boolean = false,
            hasAffinedDisplayNames: Boolean = true,
        ) : this(
            (if (hasAffinedHashes) FLAG_HAS_AFFINED_HASHES else 0)
                .or(if (hasAffinedDisplayNames) FLAG_HAS_AFFINED_DISPLAY_NAMES else 0)
                .toUByte(),
            updateNum,
            creationTime,
            clanName,
            allowUnaffined,
            talkRank.toByte(),
            kickRank.toByte(),
            lootshareRank.toByte(),
            coinshareRank.toByte(),
            affinedMembers,
            bannedMembers,
            settings,
        )

        init {
            require(affinedMembers.size <= 0xFFFF) {
                "Affined member count cannot exceed 65535"
            }
            require(bannedMembers.size <= 0xFF) {
                "Banned member count cannot exceed 255"
            }
        }

        public val flags: Int
            get() = _flags.toInt()
        public val talkRank: Int
            get() = _talkRank.toInt()
        public val kickRank: Int
            get() = _kickRank.toInt()
        public val lootshareRank: Int
            get() = _lootshareRank.toInt()
        public val coinshareRank: Int
            get() = _coinshareRank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsFullJoinUpdate

            if (_flags != other._flags) return false
            if (updateNum != other.updateNum) return false
            if (creationTime != other.creationTime) return false
            if (clanName != other.clanName) return false
            if (allowUnaffined != other.allowUnaffined) return false
            if (_talkRank != other._talkRank) return false
            if (_kickRank != other._kickRank) return false
            if (_lootshareRank != other._lootshareRank) return false
            if (_coinshareRank != other._coinshareRank) return false
            if (affinedMembers != other.affinedMembers) return false
            if (bannedMembers != other.bannedMembers) return false
            if (settings != other.settings) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _flags.hashCode()
            result = 31 * result + updateNum
            result = 31 * result + creationTime
            result = 31 * result + clanName.hashCode()
            result = 31 * result + allowUnaffined.hashCode()
            result = 31 * result + _talkRank
            result = 31 * result + _kickRank
            result = 31 * result + _lootshareRank
            result = 31 * result + _coinshareRank
            result = 31 * result + affinedMembers.hashCode()
            result = 31 * result + bannedMembers.hashCode()
            result = 31 * result + settings.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsFullJoinUpdate(" +
                "flags=$flags, " +
                "updateNum=$updateNum, " +
                "creationTime=$creationTime, " +
                "clanName='$clanName', " +
                "allowUnaffined=$allowUnaffined, " +
                "talkRank=$talkRank, " +
                "kickRank=$kickRank, " +
                "lootshareRank=$lootshareRank, " +
                "coinshareRank=$coinshareRank, " +
                "affinedMembers=$affinedMembers, " +
                "bannedMembers=$bannedMembers, " +
                "settings=$settings" +
                ")"
        }
    }

    public data object ClanSettingsFullLeaveUpdate : ClanSettingsFullUpdate

    public sealed interface ClanMember

    /**
     * An affined clan member is someone who has joined the clan permanently,
     * e.g. not as a guest.
     * @property hash the 64-bit hash of this member.
     * @property name the name of this member.
     * @property rank this member's rank in this clan
     * @property extraInfo extra information bitpacked into an integer, to be read and used
     * within clientscripts.
     * @property joinRuneDay the rune day when the member joined this clan
     * @property muted whether this member is muted in this clan.
     */
    public class AffinedClanMember private constructor(
        public val hash: Long,
        public val name: String?,
        private val _rank: Byte,
        public val extraInfo: Int,
        private val _joinRuneDay: UShort,
        public val muted: Boolean,
    ) : ClanMember {
        /**
         * Constructor for when the hash and name are both being transmitted.
         */
        public constructor(
            hash: Long,
            name: String,
            rank: Int,
            extraInfo: Int,
            joinRuneDay: Int,
            muted: Boolean,
        ) : this(
            hash,
            name,
            rank.toByte(),
            extraInfo,
            joinRuneDay.toUShort(),
            muted,
        )

        /**
         * Constructor for when only the name, and no hashes are being transmitted.
         */
        public constructor(
            name: String,
            rank: Int,
            extraInfo: Int,
            joinRuneDay: Int,
            muted: Boolean,
        ) : this(
            0,
            name,
            rank.toByte(),
            extraInfo,
            joinRuneDay.toUShort(),
            muted,
        )

        /**
         * Constructor for when only the hash and no name is being transmitted.
         */
        public constructor(
            hash: Long,
            rank: Int,
            extraInfo: Int,
            joinRuneDay: Int,
            muted: Boolean,
        ) : this(
            hash,
            null,
            rank.toByte(),
            extraInfo,
            joinRuneDay.toUShort(),
            muted,
        )

        public val rank: Int
            get() = _rank.toInt()
        public val joinRuneDay: Int
            get() = _joinRuneDay.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AffinedClanMember

            if (hash != other.hash) return false
            if (name != other.name) return false
            if (_rank != other._rank) return false
            if (extraInfo != other.extraInfo) return false
            if (_joinRuneDay != other._joinRuneDay) return false
            if (muted != other.muted) return false

            return true
        }

        override fun hashCode(): Int {
            var result = hash.hashCode()
            result = 31 * result + (name?.hashCode() ?: 0)
            result = 31 * result + _rank
            result = 31 * result + extraInfo
            result = 31 * result + _joinRuneDay.hashCode()
            result = 31 * result + muted.hashCode()
            return result
        }

        override fun toString(): String {
            return "AffinedClanMember(" +
                "hash=$hash, " +
                "name=$name, " +
                "rank=$rank, " +
                "extraInfo=$extraInfo, " +
                "joinRuneDay=$joinRuneDay, " +
                "muted=$muted" +
                ")"
        }
    }

    /**
     * A banned clan member is someone who has joined the clan permanently,
     * but has been banned from it.
     * @property hash the 64-bit hash of this member.
     * @property name the name of this member.
     */
    public class BannedClanMember(
        public val hash: Long,
        public val name: String?,
    ) : ClanMember {
        public constructor(
            hash: Long,
        ) : this(
            hash,
            null,
        )

        public constructor(
            name: String,
        ) : this(
            0,
            name,
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BannedClanMember

            if (hash != other.hash) return false
            if (name != other.name) return false

            return true
        }

        override fun hashCode(): Int {
            var result = hash.hashCode()
            result = 31 * result + (name?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return "BannedClanMember(" +
                "hash=$hash, " +
                "name=$name" +
                ")"
        }
    }

    public sealed interface ClanSetting

    /**
     * Integer-value based clan setting
     * @property id the id the of clan setting.
     * Note that the last two bits(including the sign bit) may not be used.
     * @property value the value of this setting, a 32-bit integer.
     */
    public class IntClanSetting(
        public val id: Int,
        public val value: Int,
    ) : ClanSetting {
        init {
            require(id and 0x3FFFFFFF.inv() == 0) {
                "Id cannot be larger than 30 bits"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as IntClanSetting

            if (id != other.id) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + value
            return result
        }

        override fun toString(): String {
            return "IntClanSetting(" +
                "id=$id, " +
                "value=$value" +
                ")"
        }
    }

    /**
     * Long-value based clan setting
     * @property id the id the of clan setting.
     * Note that the last two bits(including the sign bit) may not be used.
     * @property value the value of this setting, a 64-bit long.
     */
    public class LongClanSetting(
        public val id: Int,
        public val value: Long,
    ) : ClanSetting {
        init {
            require(id and 0x3FFFFFFF.inv() == 0) {
                "Id cannot be larger than 30 bits"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LongClanSetting

            if (id != other.id) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + value.hashCode()
            return result
        }

        override fun toString(): String {
            return "LongClanSetting(" +
                "id=$id, " +
                "value=$value" +
                ")"
        }
    }

    /**
     * String-value based clan setting
     * @property id the id the of clan setting.
     * Note that the last two bits(including the sign bit) may not be used.
     * @property value the value of this setting, a string.
     */
    public class StringClanSetting(
        public val id: Int,
        public val value: String,
    ) : ClanSetting {
        init {
            require(id and 0x3FFFFFFF.inv() == 0) {
                "Id cannot be larger than 30 bits"
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StringClanSetting

            if (id != other.id) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + value.hashCode()
            return result
        }

        override fun toString(): String {
            return "StringClanSetting(" +
                "id=$id, " +
                "value='$value'" +
                ")"
        }
    }

    public companion object {
        public const val FLAG_HAS_AFFINED_HASHES: Int = 0x1
        public const val FLAG_HAS_AFFINED_DISPLAY_NAMES: Int = 0x2
    }
}
