package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprot.protocol.ServerProtCategory
import net.rsprot.protocol.message.OutgoingGameMessage
import net.rsprox.protocol.game.outgoing.model.GameServerProtCategory

/**
 * Trigger on dialog abort is used to invoke any ondialogabort
 * scripts that have been set up on interfaces, typically to close
 * any dialogues.
 */
public data object TriggerOnDialogAbort : OutgoingGameMessage {
    override val category: ServerProtCategory
        get() = GameServerProtCategory.LOW_PRIORITY_PROT
}
