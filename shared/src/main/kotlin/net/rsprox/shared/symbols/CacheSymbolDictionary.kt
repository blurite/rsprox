package net.rsprox.shared.symbols

import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.api.type.GameVal
import net.rsprox.cache.api.type.GameValType
import net.rsprox.shared.ScriptVarType
import net.rsprox.shared.property.SymbolDictionary

public class CacheSymbolDictionary(
    private val cacheProvider: CacheProvider,
) : SymbolDictionary {
    private var lastLoadedCache: Cache? = null
    private var gameValTypes: Map<GameVal, Map<Int, GameValType>>? = null

    override fun getScriptVarTypeName(
        id: Int,
        type: ScriptVarType,
    ): String? {
        return when (type) {
            ScriptVarType.INTERFACE -> {
                gameValTypes?.get(GameVal.IF_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.INV -> {
                gameValTypes?.get(GameVal.INV_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.LOC -> {
                gameValTypes?.get(GameVal.LOC_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.NPC -> {
                gameValTypes?.get(GameVal.NPC_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.OBJ -> {
                gameValTypes?.get(GameVal.OBJ_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.DBROW -> {
                gameValTypes?.get(GameVal.ROW_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.SEQ -> {
                gameValTypes?.get(GameVal.SEQ_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.SPOTANIM -> {
                gameValTypes?.get(GameVal.SPOT_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.DBTABLE -> {
                gameValTypes?.get(GameVal.SPOT_TYPE)?.get(id)?.getParentOrNull()
            }
            ScriptVarType.COMPONENT -> {
                val interfaceType = gameValTypes?.get(GameVal.IF_TYPE)?.get(id ushr 16) ?: return null
                val interfaceName = interfaceType.getParentOrNull() ?: return null
                val componentName = interfaceType.getChildOrNull(id and 0xFFFF) ?: return null
                "$interfaceName:$componentName"
            }
            else -> null
        }
    }

    override fun getVarpName(id: Int): String? {
        return gameValTypes?.get(GameVal.VARP_TYPE)?.get(id)?.getParentOrNull()
    }

    override fun getVarbitName(id: Int): String? {
        return gameValTypes?.get(GameVal.VARBIT_TYPE)?.get(id)?.getParentOrNull()
    }

    override fun getScriptName(id: Int): String? {
        return null
    }

    @Synchronized
    override fun start() {
        val currentCache = cacheProvider.get()
        if (currentCache != lastLoadedCache) {
            this.lastLoadedCache = currentCache
            this.gameValTypes = currentCache.allGameValTypes()
        }
    }

    override fun stop() {
        this.lastLoadedCache = null
        this.gameValTypes = null
    }
}
