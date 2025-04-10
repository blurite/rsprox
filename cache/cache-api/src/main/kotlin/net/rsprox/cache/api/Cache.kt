package net.rsprox.cache.api

import net.rsprox.cache.api.type.GameVal
import net.rsprox.cache.api.type.GameValType
import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType

public interface Cache {
    public fun getNpcType(id: Int): NpcType?

    public fun listNpcTypes(): Collection<NpcType>

    public fun getVarBitType(id: Int): VarBitType?

    public fun listVarBitTypes(): Collection<VarBitType>

    public fun getGameValType(
        gameVal: GameVal,
        id: Int,
    ): GameValType?

    public fun listGameValTypes(gameVal: GameVal): Collection<GameValType>
}
