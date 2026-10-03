package net.rsprox.mcp.bridge

/** One connected in-client bridge. Compared by identity, so a late event from an old link cannot match a new one. */
public class BridgeLink internal constructor() {
    internal fun close() {}
}
