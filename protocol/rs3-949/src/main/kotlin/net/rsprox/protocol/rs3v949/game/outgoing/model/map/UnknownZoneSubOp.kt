package net.rsprox.protocol.rs3v949.game.outgoing.model.map

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class UnknownZoneSubOp(
    public val subOp: Int,
    public val bytes: ByteArray,
) : IncomingServerGameMessage {
    override fun toString(): String {
        return "Rs3UnknownZoneSubOp(subOp=$subOp, bytes=[${bytes.joinToString(" ") { "%02x".format(it) }}])"
    }
}
