package net.rsprox.proxy.replay

import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.api.type.GameVal
import net.rsprox.cache.api.type.GameValType
import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType

internal object ReplayCacheProvider : CacheProvider {
    override fun get(): Cache = ReplayCache
}

private object ReplayCache : Cache {
    override fun getNpcType(id: Int): NpcType? = null

    override fun listNpcTypes(): Collection<NpcType> = emptyList()

    override fun getVarBitType(id: Int): VarBitType? = null

    override fun listVarBitTypes(): Collection<VarBitType> = emptyList()

    override fun getGameValType(
        gameVal: GameVal,
        id: Int,
    ): GameValType? = null

    override fun listGameValTypes(gameVal: GameVal): Collection<GameValType> = emptyList()

    override fun allGameValTypes(): Map<GameVal, Map<Int, GameValType>> = emptyMap()
}
