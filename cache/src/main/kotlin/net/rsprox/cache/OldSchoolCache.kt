package net.rsprox.cache

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprox.cache.api.Cache
import net.rsprox.cache.api.type.NpcType
import net.rsprox.cache.api.type.VarBitType
import net.rsprox.cache.resolver.CacheResolver
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
        } catch (e: Exception) {
            this.npcs = emptyMap()
            logger.warn(e) {
                "Unable to look up npc types. Decoding may result in failures!"
            }
        }
    }

    private fun resolveVarBits() {
        check(!this::npcs.isInitialized) {
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
        } catch (e: Exception) {
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
        return Js5Compression.uncompress(indexBuf).use { uncompressedIndex ->
            Js5Compression.uncompress(groupBuf).use { uncompressedGroup ->
                Group.unpack(uncompressedGroup, checkNotNull(Js5Index.read(uncompressedIndex)[group]))
            }
        }
    }

    private companion object {
        private const val MASTER_INDEX: Int = 0xFF
        private const val CONFIG_ARCHIVE: Int = 2
        private const val NPC_GROUP: Int = 9
        private const val VARBIT_GROUP: Int = 14
        private val logger = InlineLogger()
    }
}
