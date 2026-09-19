package net.rsprox.protocol.rs3.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ClanSettingsDelta(
    public val channelIndex: Int,
    public val discardedKey: Long,
    public val revision: Int,
    public val records: List<Record>,
    public val terminator: Int,
) : IncomingServerGameMessage {
    /** Settings deltas, unlike channel deltas, are applied in forward wire order. */
    public sealed interface Record

    public data class Rejected(
        public val type: Int,
        public val sentinel: Int,
    ) : Record

    public data class AddName(
        public val type: Int,
        public val name: String,
        public val joinedDay: Int?,
    ) : Record

    public data class Rank(
        public val index: Int,
        public val rank: Int,
    ) : Record

    public data class Permissions(
        public val allowGuests: Boolean,
        public val rank0: Int,
        public val rank1: Int,
        public val rank2: Int,
        public val headerBoolean: Boolean,
    ) : Record

    public data class Remove(
        public val type: Int,
        public val index: Int,
    ) : Record

    public data class MemberBits(
        public val index: Int,
        public val value: Int,
        public val start: Int,
        public val end: Int,
    ) : Record

    public data class IntParameter(
        public val key: Int,
        public val value: Int,
    ) : Record

    public data class LongParameter(
        public val key: Int,
        public val value: Long,
    ) : Record

    public data class StringParameter(
        public val key: Int,
        public val value: String,
    ) : Record

    public data class IntBits(
        public val key: Int,
        public val value: Int,
        public val start: Int,
        public val end: Int,
    ) : Record

    public data class Name(
        public val name: String,
        public val extra: Int,
    ) : Record

    public data class Mute(
        public val index: Int,
        public val muted: Boolean,
    ) : Record
}
