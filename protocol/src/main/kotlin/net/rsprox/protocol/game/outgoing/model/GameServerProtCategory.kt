package net.rsprox.protocol.game.outgoing.model

import net.rsprot.protocol.ServerProtCategory

public enum class GameServerProtCategory(
    override val id: Int,
) : ServerProtCategory {
    HIGH_PRIORITY_PROT(0),
    LOW_PRIORITY_PROT(1),
    ;

    public companion object {
        public const val COUNT: Int = 2
    }
}
