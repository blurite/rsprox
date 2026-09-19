package net.rsprox.protocol.rs3.game.outgoing.model.info.playerinfo

import net.rsprox.cache.api.rs3.Rs3AppearanceDefinitions
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

/** Set by a successful world-login handshake, consumed only by its initial rebuild. */
public var Session.rs3PlayerInfoInitPending: Boolean? by attribute()

/** Immutable cache snapshot acquired before launching this live session. */
public var Session.rs3AppearanceDefinitions: Rs3AppearanceDefinitions? by attribute()
