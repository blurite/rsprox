package net.rsprox.protocol.game.outgoing.model

public interface IncomingZoneProt : IncomingServerGameMessage {
    /**
     * Prot id is a constant value assigned to each unique prot,
     * with the intent of being able to switch on these constants
     * and make use of a tableswitch operation, allowing fast O(1)
     * lookups for various zone prots. The respective, unique constants
     * are defined in revision-specific zone prot file.
     * Each id is expected to be unique and incrementing.
     * Gaps should not exist as they cause the JVM to use a lookupswitch instead.
     */
    public val protId: Int
}
