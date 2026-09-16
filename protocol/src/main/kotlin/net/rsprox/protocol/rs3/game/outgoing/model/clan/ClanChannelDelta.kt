package net.rsprox.protocol.rs3.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ClanChannelDelta(
    public val channelIndex: Int,
    public val discardedKey: Long,
    public val updateNumber: Long,
    public val records: List<Record>,
    public val terminator: Int,
) : IncomingServerGameMessage {
    /** Wire order is retained; the native client applies successful channel records in reverse order. */
    public sealed interface Record

    public data class Rejected(
        public val type: Int,
        public val sentinel: Int,
    ) : Record

    public data class Add(
        public val name: String,
        public val world: Int,
        public val rank: Int,
        public val memberIdentity: Long,
    ) : Record

    /** Only sentinel 255 is applied by the client; a rejected removal still consumes these fields. */
    public data class Remove(
        public val index: Int,
        public val auxiliary: Int,
        public val sentinel: Int,
    ) : Record

    public data class Header(
        public val name: String,
        public val headerBoolean: Boolean,
        public val kickRank: Int,
        public val talkRank: Int,
    ) : Record

    public data class Member(
        public val unused: Int,
        public val index: Int,
        public val rank: Int,
        public val world: Int,
        public val memberIdentity: Long,
        public val name: String,
        public val memberBoolean: Boolean,
    ) : Record
}
