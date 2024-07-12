package net.rsprox.protocol.game.outgoing.model.info.npcinfo

public enum class MoveSpeed(
    public val id: Int,
) {
    STATIONARY(-1),
    CRAWL(0),
    WALK(1),
    RUN(2),
}
