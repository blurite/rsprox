package net.rsprox.protocol.rs3.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Exactly 24 UID octets and their CRC; invalid updates are reported but not applied by the client. */
public data class UpdateUid192(
    public val uid: List<Int>,
    public val crc: Int,
    public val valid: Boolean,
) : IncomingServerGameMessage
