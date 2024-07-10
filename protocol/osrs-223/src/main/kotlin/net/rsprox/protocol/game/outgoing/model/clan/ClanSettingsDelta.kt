package net.rsprox.protocol.game.outgoing.model.clan

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Clan settings delta updates are used to modify a sub-set of this clan's settings.
 * @property clanType the type of the clan to modify, e.g. guest or normal,
 * @property owner the hash of the owner.
 * As the value of this property is never assigned in the client, but it is compared,
 * this property should always be assigned the value 0.
 * @property updateNum the number of updates this clans settings has had.
 * If the value does not match up, the client will throw an exception!
 */
public class ClanSettingsDelta private constructor(
    private val _clanType: Byte,
    public val owner: Long,
    public val updateNum: Int,
    public val updates: List<ClanSettingsDeltaUpdate>,
) : OutgoingGameMessage {
    public constructor(
        clanType: Int,
        owner: Long,
        updateNum: Int,
        updates: List<ClanSettingsDeltaUpdate>,
    ) : this(
        clanType.toByte(),
        owner,
        updateNum,
        updates,
    )

    public val clanType: Int
        get() = _clanType.toInt()
    override val category: ServerProtCategory
        get() = GameServerProtCategory.HIGH_PRIORITY_PROT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClanSettingsDelta

        if (_clanType != other._clanType) return false
        if (owner != other.owner) return false
        if (updateNum != other.updateNum) return false
        if (updates != other.updates) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _clanType.toInt()
        result = 31 * result + owner.hashCode()
        result = 31 * result + updateNum
        return result
    }

    override fun toString(): String {
        return "ClanSettingsDelta(" +
            "clanType=$clanType, " +
            "owner=$owner, " +
            "updateNum=$updateNum, " +
            "updates=$updates" +
            ")"
    }

    public sealed interface ClanSettingsDeltaUpdate

    /**
     * Add banned updates are used to add a member to the banned members list.
     * @property hash the hash of the member, or 0 if this clan does not use hashes.
     * @property name the name of the member.
     */
    public class ClanSettingsDeltaAddBannedUpdate(
        public val hash: Long,
        public val name: String?,
    ) : ClanSettingsDeltaUpdate {
        /**
         * A secondary constructor for when the clan does not support hashes.
         */
        public constructor(
            name: String,
        ) : this(
            0,
            name,
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaAddBannedUpdate

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
            return "ClanSettingsDeltaAddBannedUpdate(" +
                "hash=$hash, " +
                "name=$name" +
                ")"
        }
    }

    /**
     * Older add-member update for clans.
     * @property hash the hash of the member, or 0 if this clan does not use hashes.
     * @property name the name of the member.
     */
    public class ClanSettingsDeltaAddMemberV1Update(
        public val hash: Long,
        public val name: String?,
    ) : ClanSettingsDeltaUpdate {
        /**
         * A secondary constructor for when the clan does not support hashes.
         */
        public constructor(
            name: String,
        ) : this(
            0,
            name,
        )

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaAddMemberV1Update

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
            return "ClanSettingsDeltaAddMemberV1Update(" +
                "hash=$hash, " +
                "name=$name" +
                ")"
        }
    }

    /**
     * Newer add-member update for clans.
     * @property hash the hash of the member, or 0 if this clan does not use hashes.
     * @property name the name of the member.
     * @property joinRuneDay the rune day when this user joined the clan
     */
    public class ClanSettingsDeltaAddMemberV2Update private constructor(
        public val hash: Long,
        public val name: String?,
        private val _joinRuneDay: UShort,
    ) : ClanSettingsDeltaUpdate {
        public constructor(
            hash: Long,
            name: String?,
            joinRuneDay: Int,
        ) : this(
            hash,
            name,
            joinRuneDay.toUShort(),
        )

        public constructor(
            name: String?,
            joinRuneDay: Int,
        ) : this(
            0,
            name,
            joinRuneDay.toUShort(),
        )

        public val joinRuneDay: Int
            get() = _joinRuneDay.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaAddMemberV2Update

            if (hash != other.hash) return false
            if (name != other.name) return false
            if (_joinRuneDay != other._joinRuneDay) return false

            return true
        }

        override fun hashCode(): Int {
            var result = hash.hashCode()
            result = 31 * result + (name?.hashCode() ?: 0)
            result = 31 * result + _joinRuneDay.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaAddMemberV2Update(" +
                "hash=$hash, " +
                "name=$name, " +
                "joinRuneDay=$joinRuneDay" +
                ")"
        }
    }

    /**
     * Base settings updates are used to manage global clan settings,
     * such as privileges to use various aspects of this clan.
     * @property allowUnaffined whether guest members are allowed to join this clan
     * @property talkRank the minimum rank needed to talk within this clan
     * @property kickRank the minimum rank needed to kick other members in this clan
     * @property lootshareRank the minimum rank needed to toggle lootshare, unused in OldSchool
     * @property coinshareRank the minimum rank needed to toggle coinshare, unused in OldSchool
     */
    public class ClanSettingsDeltaBaseSettingsUpdate private constructor(
        public val allowUnaffined: Boolean,
        private val _talkRank: Byte,
        private val _kickRank: Byte,
        private val _lootshareRank: Byte,
        private val _coinshareRank: Byte,
    ) : ClanSettingsDeltaUpdate {
        public constructor(
            allowUnaffined: Boolean,
            talkRank: Int,
            kickRank: Int,
            lootshareRank: Int,
            coinshareRank: Int,
        ) : this(
            allowUnaffined,
            talkRank.toByte(),
            kickRank.toByte(),
            lootshareRank.toByte(),
            coinshareRank.toByte(),
        )

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

            other as ClanSettingsDeltaBaseSettingsUpdate

            if (allowUnaffined != other.allowUnaffined) return false
            if (_talkRank != other._talkRank) return false
            if (_kickRank != other._kickRank) return false
            if (_lootshareRank != other._lootshareRank) return false
            if (_coinshareRank != other._coinshareRank) return false

            return true
        }

        override fun hashCode(): Int {
            var result = allowUnaffined.hashCode()
            result = 31 * result + _talkRank
            result = 31 * result + _kickRank
            result = 31 * result + _lootshareRank
            result = 31 * result + _coinshareRank
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaBaseSettingsUpdate(" +
                "allowUnaffined=$allowUnaffined, " +
                "talkRank=$talkRank, " +
                "kickRank=$kickRank, " +
                "lootshareRank=$lootshareRank, " +
                "coinshareRank=$coinshareRank" +
                ")"
        }
    }

    /**
     * Delete banned member updates are used to remove existing banned members
     * from the list of banned users.
     * @property index the index of the user in the banned members list.
     */
    public class ClanSettingsDeltaDeleteBannedUpdate(
        public val index: Int,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaDeleteBannedUpdate

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "ClanSettingsDeltaDeleteBannedUpdate(index=$index)"
        }
    }

    /**
     * Delete member updates are used to remove members from this clan.
     * @property index the index of this member within the clan's member list.
     */
    public class ClanSettingsDeltaDeleteMemberUpdate(
        public val index: Int,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaDeleteMemberUpdate

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "ClanSettingsDeltaDeleteMemberUpdate(index=$index)"
        }
    }

    /**
     * Set member rank update is used to modify a given clan member's privileges
     * within the clan.
     * @property index the index of this member within the clan's member list.
     * @property rank the new rank to assign to that member.
     */
    public class ClanSettingsDeltaSetMemberRankUpdate private constructor(
        private val _index: UShort,
        private val _rank: Byte,
    ) : ClanSettingsDeltaUpdate {
        public constructor(
            index: Int,
            rank: Int,
        ) : this(
            index.toUShort(),
            rank.toByte(),
        )

        public val index: Int
            get() = _index.toInt()
        public val rank: Int
            get() = _rank.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetMemberRankUpdate

            if (_index != other._index) return false
            if (_rank != other._rank) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _index.hashCode()
            result = 31 * result + _rank
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetMemberRankUpdate(" +
                "index=$index, " +
                "rank=$rank" +
                ")"
        }
    }

    /**
     * Set member extra info is used to modify extra info about a member in the clan,
     * by modifying the provided bit range of the 32-bit integer that each
     * member has.
     * @property index the index of this member in the clan's members list.
     * @property value the value to assign to the provided bit range
     * @property startBit the start bit of the bit range to update
     * @property endBit the end bit of the bit range to update
     */
    public class ClanSettingsDeltaSetMemberExtraInfoUpdate private constructor(
        private val _index: UShort,
        public val value: Int,
        private val _startBit: UByte,
        private val _endBit: UByte,
    ) : ClanSettingsDeltaUpdate {
        public constructor(
            index: Int,
            value: Int,
            startBit: Int,
            endBit: Int,
        ) : this(
            index.toUShort(),
            value,
            startBit.toUByte(),
            endBit.toUByte(),
        )

        public val index: Int
            get() = _index.toInt()
        public val startBit: Int
            get() = _startBit.toInt()
        public val endBit: Int
            get() = _endBit.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetMemberExtraInfoUpdate

            if (_index != other._index) return false
            if (value != other.value) return false
            if (_startBit != other._startBit) return false
            if (_endBit != other._endBit) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _index.hashCode()
            result = 31 * result + value
            result = 31 * result + _startBit.hashCode()
            result = 31 * result + _endBit.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetMemberExtraInfoUpdate(" +
                "index=$index, " +
                "value=$value, " +
                "startBit=$startBit, " +
                "endBit=$endBit" +
                ")"
        }
    }

    /**
     * Set member muted updates are used to mute or unmute members of this clan.
     * @property index the index of this member within the clan's member list.
     * @property muted whether to set the member muted or unmuted.
     */
    public class ClanSettingsDeltaSetMemberMutedUpdate private constructor(
        private val _index: UShort,
        public val muted: Boolean,
    ) : ClanSettingsDeltaUpdate {
        public constructor(
            index: Int,
            muted: Boolean,
        ) : this(
            index.toUShort(),
            muted,
        )

        public val index: Int
            get() = _index.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetMemberMutedUpdate

            if (_index != other._index) return false
            if (muted != other.muted) return false

            return true
        }

        override fun hashCode(): Int {
            var result = _index.hashCode()
            result = 31 * result + muted.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetMemberMutedUpdate(" +
                "index=$index, " +
                "muted=$muted" +
                ")"
        }
    }

    /**
     * Int setting updates are used to modify the value of an integer-based
     * setting of this clan.
     * @property setting the id of the setting to modify, a 30-bit integer.
     * @property value the 32-bit integer value to assign to that setting.
     */
    public class ClanSettingsDeltaSetIntSettingUpdate(
        public val setting: Int,
        public val value: Int,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetIntSettingUpdate

            if (setting != other.setting) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = setting
            result = 31 * result + value
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetIntSettingUpdate(" +
                "setting=$setting, " +
                "value=$value" +
                ")"
        }
    }

    /**
     * Long setting updates are used to modify the value of a long-based
     * setting of this clan.
     * @property setting the id of the setting to modify, a 30-bit integer.
     * @property value the 64-bit long value to assign to that setting.
     */
    public class ClanSettingsDeltaSetLongSettingUpdate(
        public val setting: Int,
        public val value: Long,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetLongSettingUpdate

            if (setting != other.setting) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = setting
            result = 31 * result + value.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetLongSettingUpdate(" +
                "setting=$setting, " +
                "value=$value" +
                ")"
        }
    }

    /**
     * String setting updates are used to modify the values of string settings
     * within the clan.
     * @property setting the id of the setting to modify, a 30-bit integer.
     * @property value the string value to assign to that setting.
     */
    public class ClanSettingsDeltaSetStringSettingUpdate(
        public val setting: Int,
        public val value: String,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetStringSettingUpdate

            if (setting != other.setting) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = setting
            result = 31 * result + value.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetStringSettingUpdate(" +
                "setting=$setting, " +
                "value='$value'" +
                ")"
        }
    }

    /**
     * Varbit setting updates are used to modify a bit-range of an integer-based
     * setting of this clan.
     * @property setting the id of the setting to modify, a 30-bit integer.
     * @property value the new value to assign to the provided bit range.
     * @property startBit the start bit of the bit range to modify
     * @property endBit the end bit of the bit range ot modify
     */
    public class ClanSettingsDeltaSetVarbitSettingUpdate private constructor(
        public val setting: Int,
        public val value: Int,
        private val _startBit: UByte,
        private val _endBit: UByte,
    ) : ClanSettingsDeltaUpdate {
        public constructor(
            setting: Int,
            value: Int,
            startBit: Int,
            endBit: Int,
        ) : this(
            setting,
            value,
            startBit.toUByte(),
            endBit.toUByte(),
        )

        public val startBit: Int
            get() = _startBit.toInt()
        public val endBit: Int
            get() = _endBit.toInt()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetVarbitSettingUpdate

            if (setting != other.setting) return false
            if (value != other.value) return false
            if (_startBit != other._startBit) return false
            if (_endBit != other._endBit) return false

            return true
        }

        override fun hashCode(): Int {
            var result = setting
            result = 31 * result + value
            result = 31 * result + _startBit.hashCode()
            result = 31 * result + _endBit.hashCode()
            return result
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetVarbitSettingUpdate(" +
                "setting=$setting, " +
                "value=$value, " +
                "startBit=$startBit, " +
                "endBit=$endBit" +
                ")"
        }
    }

    /**
     * Clan name updates are used to modify the name of the clan.
     * @property clanName the new clan name to assign to this clan.
     */
    public class ClanSettingsDeltaSetClanNameUpdate(
        public val clanName: String,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingsDeltaSetClanNameUpdate

            return clanName == other.clanName
        }

        override fun hashCode(): Int {
            return clanName.hashCode()
        }

        override fun toString(): String {
            return "ClanSettingsDeltaSetClanNameUpdate(clanName='$clanName')"
        }
    }

    /**
     * Clan owner updates are used to assign a new owner to this clan.
     * @property index the index of the new owner in the clan's members list.
     */
    public class ClanSettingDeltaSetClanOwnerUpdate(
        public val index: Int,
    ) : ClanSettingsDeltaUpdate {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ClanSettingDeltaSetClanOwnerUpdate

            return index == other.index
        }

        override fun hashCode(): Int {
            return index
        }

        override fun toString(): String {
            return "ClanSettingDeltaSetClanOwnerUpdate(index=$index)"
        }
    }
}
