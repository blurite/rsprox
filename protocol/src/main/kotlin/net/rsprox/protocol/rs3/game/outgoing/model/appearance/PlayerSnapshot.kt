package net.rsprox.protocol.rs3.game.outgoing.model.appearance

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

public data class PlayerSnapshot(
    public val snapshotIndex: Int,
    public val appearanceFlags: Int,
    public val appearance: AppearanceBody,
) : IncomingServerGameMessage
