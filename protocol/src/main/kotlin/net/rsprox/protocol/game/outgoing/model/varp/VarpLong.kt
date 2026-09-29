package net.rsprox.protocol.game.outgoing.model.varp

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Revision 241's 64-bit varp value. Do not truncate it into the legacy Int varp model. */
public data class VarpLong(public val id: Int, public val value: Long) : IncomingServerGameMessage
