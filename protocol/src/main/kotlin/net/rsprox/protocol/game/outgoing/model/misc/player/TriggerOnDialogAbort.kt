package net.rsprox.protocol.game.outgoing.model.misc.player

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/**
 * Trigger on dialog abort is used to invoke any ondialogabort
 * scripts that have been set up on interfaces, typically to close
 * any dialogues.
 */
public data object TriggerOnDialogAbort : IncomingServerGameMessage
