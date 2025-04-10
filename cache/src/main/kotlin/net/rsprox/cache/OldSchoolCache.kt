package net.rsprox.cache

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.type.GameVal
import net.rsprox.cache.api.type.GameValType
import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.cache.resolver.CacheResolver
import net.rsprox.cache.type.OldSchoolGameValType
import net.rsprox.cache.type.OldSchoolNpcType
import net.rsprox.cache.type.OldSchoolVarBitType
import org.openrs2.buffer.use
import org.openrs2.cache.Group
import org.openrs2.cache.Js5Compression
import org.openrs2.cache.Js5Index
import kotlin.time.measureTime

public class OldSchoolCache(
    private val resolver: CacheResolver,
    private val masterIndex: Js5MasterIndex,
) : Cache {
    private lateinit var npcs: Map<Int, NpcType>
    private lateinit var gameVals: Map<GameVal, Map<Int, GameValType>>
    private lateinit var varbits: Map<Int, VarBitType>

    override fun getNpcType(id: Int): NpcType? {
        if (!this::npcs.isInitialized) {
            resolveNpcs()
        }
        return npcs[id]
    }

    override fun listNpcTypes(): Collection<NpcType> {
        if (!this::npcs.isInitialized) {
            resolveNpcs()
        }
        return this.npcs.values
    }

    override fun getVarBitType(id: Int): VarBitType? {
        if (!this::varbits.isInitialized) {
            resolveVarBits()
        }
        return varbits[id]
    }

    override fun listVarBitTypes(): Collection<VarBitType> {
        if (!this::varbits.isInitialized) {
            resolveVarBits()
        }
        return this.varbits.values
    }

    private fun resolveNpcs() {
        check(!this::npcs.isInitialized) {
            "Npcs already initialized."
        }
        try {
            val buffers = getFiles(CONFIG_ARCHIVE, NPC_GROUP)
            logger.debug { "Decoding ${buffers.size} npc types." }
            val time =
                measureTime {
                    this.npcs =
                        buffers.entries.associate { (id, buffer) ->
                            val decoded =
                                try {
                                    OldSchoolNpcType.get(masterIndex.revision, id, buffer.toJagByteBuf())
                                } finally {
                                    buffer.release()
                                }
                            id to decoded
                        }
                }
            logger.debug { "${buffers.size} npc types decoded in $time" }
        } catch (e: Throwable) {
            this.npcs = emptyMap()
            logger.warn(e) {
                "Unable to look up npc types. Decoding may result in failures!"
            }
        }
    }

    override fun getGameValType(
        gameVal: GameVal,
        id: Int,
    ): GameValType? {
        if (!this::gameVals.isInitialized) {
            resolveGameVals()
        }
        return gameVals[gameVal]?.get(id)
    }

    override fun listGameValTypes(gameVal: GameVal): Collection<GameValType> {
        if (!this::gameVals.isInitialized) {
            resolveGameVals()
        }
        return gameVals[gameVal]?.values ?: emptyList()
    }

    override fun allGameValTypes(): Map<GameVal, Map<Int, GameValType>> {
        if (!this::gameVals.isInitialized) {
            resolveGameVals()
        }
        return this.gameVals
    }

    private fun resolveGameVals() {
        check(!this::gameVals.isInitialized) {
            "Game vals already initialized."
        }
        // Gamevals were only introduced in revision 230
        if (masterIndex.revision < 230) {
            this.gameVals = emptyMap()
            return
        }
        try {
            val gameValMap = mutableMapOf<GameVal, Map<Int, GameValType>>()
            this.gameVals = gameValMap
            val totalTime =
                measureTime {
                    for (gameVal in GameVal.entries) {
                        val groupId = getGameValGroupId(gameVal)
                        val buffers = getFiles(GAMEVAL_ARCHIVE, groupId)
                        logger.debug { "Decoding ${buffers.size} gameval types." }
                        val time =
                            measureTime {
                                gameValMap[gameVal] =
                                    buffers.entries.associate { (id, buffer) ->
                                        val decoded =
                                            try {
                                                OldSchoolGameValType.get(
                                                    masterIndex.revision,
                                                    gameVal,
                                                    id,
                                                    buffer.toJagByteBuf(),
                                                )
                                            } finally {
                                                buffer.release()
                                            }
                                        id to decoded
                                    }
                            }
                        logger.debug { "${buffers.size} $gameVal gamevals decoded in $time" }
                    }
                }
            logger.debug { "Decoded ${this.gameVals.size} gameval types in $totalTime" }
        } catch (e: Throwable) {
            logger.warn(e) {
                "Unable to look up gameval types. Names may not be supported for everything."
            }
        }
    }

    private fun getGameValGroupId(gameVal: GameVal): Int {
        return when (gameVal) {
            GameVal.IF_TYPE -> GAMEVAL_IF_TYPES
            GameVal.INV_TYPE -> GAMEVAL_INV_TYPES
            GameVal.LOC_TYPE -> GAMEVAL_LOC_TYPES
            GameVal.NPC_TYPE -> GAMEVAL_NPC_TYPES
            GameVal.OBJ_TYPE -> GAMEVAL_OBJ_TYPES
            GameVal.ROW_TYPE -> GAMEVAL_ROW_TYPES
            GameVal.SEQ_TYPE -> GAMEVAL_SEQ_TYPES
            GameVal.SPOT_TYPE -> GAMEVAL_SPOT_TYPES
            GameVal.TABLE_TYPE -> GAMEVAL_TABLE_TYPES
            GameVal.VARBIT_TYPE -> GAMEVAL_VARBIT_TYPES
            GameVal.VARP_TYPE -> GAMEVAL_VARP_TYPES
        }
    }

    private fun resolveVarBits() {
        check(!this::varbits.isInitialized) {
            "VarBits already initialized."
        }
        try {
            val buffers = getFiles(CONFIG_ARCHIVE, VARBIT_GROUP)
            logger.debug { "Decoding ${buffers.size} varbit types." }
            val time =
                measureTime {
                    this.varbits =
                        buffers.entries.associate { (id, buffer) ->
                            val decoded =
                                try {
                                    OldSchoolVarBitType.get(masterIndex.revision, id, buffer.toJagByteBuf())
                                } finally {
                                    buffer.release()
                                }
                            id to decoded
                        }
                }
            logger.debug {
                "${buffers.size} varbits decoded in $time"
            }
        } catch (e: Throwable) {
            this.varbits = emptyMap()
            logger.debug(e) {
                "Unable to look up var bit types. Decoding will be unaware of affected varbits."
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun getFiles(
        archive: Int,
        group: Int,
    ): Map<Int, ByteBuf> {
        val indexBuf = checkNotNull(resolver.get(masterIndex, MASTER_INDEX, archive))
        val groupBuf = checkNotNull(resolver.get(masterIndex, archive, group))
        try {
            return Js5Compression.uncompress(indexBuf).use { uncompressedIndex ->
                Js5Compression.uncompress(groupBuf).use { uncompressedGroup ->
                    Group.unpack(uncompressedGroup, checkNotNull(Js5Index.read(uncompressedIndex)[group]))
                }
            }
        } finally {
            indexBuf.release()
            groupBuf.release()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OldSchoolCache

        return masterIndex == other.masterIndex
    }

    override fun hashCode(): Int {
        return masterIndex.hashCode()
    }

    override fun toString(): String {
        return "OldSchoolCache(masterIndex=$masterIndex)"
    }

    private companion object {
        private val logger = InlineLogger()
        private const val MASTER_INDEX: Int = 0xFF
        private const val CONFIG_ARCHIVE: Int = 2
        private const val GAMEVAL_ARCHIVE: Int = 24
        private const val NPC_GROUP: Int = 9
        private const val VARBIT_GROUP: Int = 14
        private const val GAMEVAL_OBJ_TYPES: Int = 0
        private const val GAMEVAL_NPC_TYPES: Int = 1
        private const val GAMEVAL_INV_TYPES: Int = 2
        private const val GAMEVAL_VARP_TYPES: Int = 3
        private const val GAMEVAL_VARBIT_TYPES: Int = 4
        private const val GAMEVAL_LOC_TYPES: Int = 6
        private const val GAMEVAL_SEQ_TYPES: Int = 7
        private const val GAMEVAL_SPOT_TYPES: Int = 8
        private const val GAMEVAL_ROW_TYPES: Int = 9
        private const val GAMEVAL_TABLE_TYPES: Int = 10
        private const val GAMEVAL_IF_TYPES: Int = 13

        // Sound types unusable as jingles and midis are mixed with randomized ids in them
        @Suppress("unused")
        private const val GAMEVAL_SOUND_TYPES: Int = 11

        // Sprite types unusable as the ids are randomized
        @Suppress("unused")
        private const val GAMEVAL_SPRITE_TYPES: Int = 12
    }
}
