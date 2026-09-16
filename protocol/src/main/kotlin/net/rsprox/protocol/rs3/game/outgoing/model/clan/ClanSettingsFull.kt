package net.rsprox.protocol.rs3.game.outgoing.model.clan

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class ClanSettingsFull(
    public val channelIndex: Int,
    public val settings: Settings?,
) : IncomingServerGameMessage {
    /** Legacy timestamp is the transmitted value, before the version <4 client-side epoch adjustment. */
    public data class Settings(
        public val version: Int,
        public val flags: Int,
        public val updateNumber: Int,
        public val legacyTimestamp: Int,
        public val name: String,
        public val extra: Int?,
        public val allowGuests: Boolean,
        public val rank0: Int,
        public val rank1: Int,
        public val rank2: Int,
        public val headerBoolean: Boolean,
        public val members: List<Member>,
        public val banned: List<String>,
        public val parameters: List<Parameter>,
    )

    public data class Member(
        public val name: String,
        public val rank: Int,
        public val bits: Int?,
        public val joinedDay: Int?,
        public val muted: Boolean?,
    )

    public sealed interface Parameter {
        public val key: Int
    }

    public data class IntParameter(
        override val key: Int,
        public val value: Int,
    ) : Parameter

    public data class LongParameter(
        override val key: Int,
        public val value: Long,
    ) : Parameter

    public data class StringParameter(
        override val key: Int,
        public val value: String,
    ) : Parameter

    public data class NullParameter(
        override val key: Int,
    ) : Parameter
}
