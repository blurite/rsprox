package net.rsprox.protocol.rs3.game.outgoing.model.world

import net.rsprox.protocol.game.outgoing.model.IncomingServerGameMessage

/** Intermediate chunks have no decoded list until completeFlag == 1. */
public data class WorldlistFetchReply(
    public val completeFlag: Int,
    public val chunkLength: Int,
    public val accumulatedLength: Int,
    public val reply: Reply?,
) : IncomingServerGameMessage {
    public data class Reply(
        public val version: Int,
        public val definitionsFlag: Int,
        public val definitions: Definitions?,
        public val populations: List<Population>,
    )

    public data class Definitions(
        public val countries: List<Country>,
        public val minimumWorldId: Int,
        public val maximumWorldId: Int,
        public val worlds: List<World>,
        public val checksum: Int,
    )

    public data class MarkedString(
        public val marker: Int,
        public val text: String?,
    )

    public data class Country(
        public val id: Int,
        public val name: MarkedString,
    )

    public data class World(
        public val index: Int,
        public val countryIndex: Int,
        public val properties: Int,
        public val extraId: Int,
        public val extraName: MarkedString?,
        public val activity: MarkedString,
        public val hostname: MarkedString,
    )

    /** Indices are transmitted relative to minimumWorldId; 65535 population is offline (-1). */
    public data class Population(
        public val index: Int,
        public val population: Int,
    )
}
