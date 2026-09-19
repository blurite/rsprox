package net.rsprox.protocol.rs3.cache

import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

/** Shared by both directions of a live connection; lookups never block on cache I/O. */
public var Session.rs3PacketDefinitions: Rs3PacketDefinitions? by attribute()
