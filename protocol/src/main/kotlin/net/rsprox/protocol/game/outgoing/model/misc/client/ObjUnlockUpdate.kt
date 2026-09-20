package net.rsprox.protocol.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Each entry replaces a 64-object unlock word; it is not an OR operation. */
public data class ObjUnlockUpdate(public val entries: List<Entry>) : IncomingServerGameMessage {
    public data class Entry(public val wordIndex: Int, public val flags: Long)
}
