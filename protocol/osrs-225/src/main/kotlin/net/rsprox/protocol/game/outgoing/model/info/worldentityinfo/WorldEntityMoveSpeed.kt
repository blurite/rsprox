package net.rsprox.protocol.game.outgoing.model.info.worldentityinfo

public enum class WorldEntityMoveSpeed(
    public val id: Int,
) {
    ZERO(-1),
    ZERO_POINT_FIVE(0),
    ONE(1),
    ONE_POINT_FIVE(2),
    TWO(3),
    TWO_POINT_FIVE(4),
    THREE(5),
    THREE_POINT_FIVE(6),
    FOUR(7),
    ;

    public companion object {
        public operator fun get(id: Int): WorldEntityMoveSpeed {
            return entries.first { it.id == id }
        }
    }
}
