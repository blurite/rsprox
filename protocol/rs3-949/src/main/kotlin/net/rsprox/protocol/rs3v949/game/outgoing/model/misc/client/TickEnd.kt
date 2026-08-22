package net.rsprox.protocol.rs3v949.game.outgoing.model.misc.client

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public class TickEnd : IncomingServerGameMessage {
    override fun toString(): String = "TickEnd"
}
