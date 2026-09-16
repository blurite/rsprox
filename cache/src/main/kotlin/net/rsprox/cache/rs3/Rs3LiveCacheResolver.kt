package net.rsprox.cache.rs3

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.Unpooled
import net.rsprox.cache.CACHES_DIRECTORY
import net.rsprox.cache.api.rs3.Rs3AppearanceItem
import net.rsprox.cache.api.rs3.Rs3NpcDefinition
import net.rsprox.cache.api.rs3.Rs3PacketDefinitions
import net.rsprox.cache.api.rs3.Rs3QuickChatPhrase
import net.rsprox.cache.api.rs3.Rs3VarbitDefinition
import net.rsprox.cache.api.rs3.Rs3VariableDefinition
import net.rsprox.cache.api.rs3.Rs3VariableDomain
import org.openrs2.buffer.use
import org.openrs2.cache.Group
import org.openrs2.cache.Js5Compression
import org.openrs2.cache.Js5Index
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.concurrent.ConcurrentHashMap
import java.util.zip.CRC32

/** Resolves a live snapshot before gameplay. The resulting lookups never perform network I/O. */
public class Rs3LiveCacheResolver(
    private val info: Rs3Js5ConnectionInfo,
    private val directory: Path = CACHES_DIRECTORY.resolve("rs3-live"),
) {
    public fun loadPacketDefinitions(): Rs3PacketDefinitions {
        require(info.revision == 950) { "RS3 definition decoding is currently verified for revision 950 only" }
        val started = System.nanoTime()
        Rs3Js5Connection(info).use { connection ->
            val master = unpack(connection.get(255, 255))
            val metadata = ByteBuffer.wrap(master)
            val archiveCount = metadata.get().toInt() and 255
            require(archiveCount > 28 && metadata.remaining() >= archiveCount * 80) { "Truncated RS3 master index" }
            val archives =
                List(archiveCount) {
                    val crc = metadata.int
                    val version = metadata.int
                    metadata.position(metadata.position() + 72) // group count, length and Whirlpool digest
                    crc to version
                }

            fun index(archive: Int): Js5Index {
                val (crc, version) = archives[archive]
                val bytes = readGroup(connection, 255, archive, version, crc)
                return Unpooled.wrappedBuffer(unpack(bytes)).use { buffer ->
                    Js5Index.read(buffer).also {
                        require(it.version == version && !buffer.isReadable) { "Invalid RS3 archive index $archive" }
                    }
                }
            }

            fun files(
                archive: Int,
                group: Js5Index.MutableGroup,
            ): Map<Int, ByteArray> {
                val bytes = readGroup(connection, archive, group.id, group.version, group.checksum)
                return Unpooled.wrappedBuffer(unpack(bytes)).use { data ->
                    val decoded = Group.unpack(data, group)
                    try {
                        decoded.mapValues { (_, value) ->
                            ByteArray(value.readableBytes()).also { value.readBytes(it) }
                        }
                    } finally {
                        decoded.values.forEach { it.release() }
                    }
                }
            }
            val defaults = files(28, checkNotNull(index(28)[6]) { "Missing wear-position defaults group" })
            val slots = decodeWearPositions(checkNotNull(defaults[0]) { "Missing wear-position defaults file" })
            val items = HashMap<Int, ByteArray>()
            val itemIndex = index(19)
            logger.info { "Loading RS3 packet definitions: ${itemIndex.size} item groups (no model assets)" }
            for (group in itemIndex) {
                for ((file, bytes) in files(19, group)) {
                    require(file in 0..255) { "Invalid RS3 item file ID: $file" }
                    items[(group.id shl 8) or file] = bytes
                }
            }
            val phrases =
                files(24, checkNotNull(index(24)[1]) { "Missing quickchat phrases" })
                    .mapValues { (_, bytes) -> Rs3PacketDefinitionDecoder.phrase(bytes) }
            val configIndex = index(2)
            val varbits =
                files(2, checkNotNull(configIndex[69]) { "Missing varbit definitions" })
                    .mapValues { (_, bytes) -> Rs3PacketDefinitionDecoder.varbit(bytes) }
            val variables =
                Rs3VariableDomain.entries.associateWith { domain ->
                    files(2, checkNotNull(configIndex[domain.group]) { "Missing $domain definitions" })
                        .mapValues { (_, bytes) -> Rs3PacketDefinitionDecoder.variable(bytes) }
                }
            val npcs = HashMap<Int, ByteArray>()
            for (group in index(18)) {
                for ((file, bytes) in files(18, group)) {
                    require(file in 0..127) { "Invalid RS3 NPC file ID: $file" }
                    npcs[(group.id shl 7) or file] = bytes
                }
            }
            require(master.contentEquals(unpack(connection.get(255, 255)))) {
                "RS3 cache changed during bootstrap; please launch again"
            }
            logger.info {
                "RS3 packet cache ready: ${slots.size} slots, ${items.size} items, " +
                    "${npcs.size} NPCs, ${phrases.size} phrases, " +
                    "${(System.nanoTime() - started) / 1_000_000} ms"
            }
            return Snapshot(slots, items, phrases, variables, npcs, varbits)
        }
    }

    private class Snapshot(
        override val equipmentSlotKinds: List<Int>,
        private val items: Map<Int, ByteArray>,
        private val phrases: Map<Int, Rs3QuickChatPhrase>,
        private val variables: Map<Rs3VariableDomain, Map<Int, Rs3VariableDefinition>>,
        private val npcs: Map<Int, ByteArray>,
        private val varbits: Map<Int, Rs3VarbitDefinition>,
    ) : Rs3PacketDefinitions {
        private val decodedNpcs = ConcurrentHashMap<Int, Rs3NpcDefinition>()

        override fun getVarbit(id: Int): Rs3VarbitDefinition =
            checkNotNull(varbits[id]) { "Missing RS3 varbit definition $id" }

        override fun getNpc(id: Int): Rs3NpcDefinition =
            decodedNpcs.computeIfAbsent(id) {
                Rs3NpcDefinitionDecoder.decode(id, checkNotNull(npcs[id]) { "Missing RS3 NPC definition $id" })
            }

        override fun getQuickChatPhrase(id: Int): Rs3QuickChatPhrase =
            checkNotNull(phrases[id]) { "Missing RS3 quickchat phrase $id" }

        override fun getVariable(
            domain: Rs3VariableDomain,
            id: Int,
        ): Rs3VariableDefinition = checkNotNull(variables[domain]?.get(id)) { "Missing RS3 $domain variable $id" }

        private val decoded = ConcurrentHashMap<Int, Rs3AppearanceItem>()

        override fun getItem(id: Int): Rs3AppearanceItem = resolveItem(id, HashSet())

        private fun resolveItem(
            id: Int,
            resolving: MutableSet<Int>,
        ): Rs3AppearanceItem {
            decoded[id]?.let { return it }
            check(resolving.size < 64 && resolving.add(id)) { "Cyclic/deep RS3 item template at $id" }
            try {
                val result =
                    Rs3AppearanceItemDecoder.decode(
                        id,
                        checkNotNull(items[id]) { "Missing RS3 item definition $id" },
                    ) { link -> resolveItem(link, resolving) }
                return decoded.putIfAbsent(id, result) ?: result
            } finally {
                resolving.remove(id)
            }
        }
    }

    private fun readGroup(
        connection: Rs3Js5Connection,
        archive: Int,
        group: Int,
        version: Int,
        crc: Int,
    ): ByteArray {
        val path = directory.resolve("$archive/${group}_${version}_${crc.toUInt().toString(16)}.dat")
        if (Files.isRegularFile(path)) {
            val cached = Files.readAllBytes(path)
            if (checksum(cached) == crc) return cached
            logger.warn { "Ignoring corrupt RS3 cached group $archive:$group" }
        }
        val bytes = connection.get(archive, group)
        require(checksum(bytes) == crc) { "RS3 JS5 checksum mismatch for $archive:$group; cache may have updated" }
        Files.createDirectories(path.parent)
        val temporary = Files.createTempFile(path.parent, "js5-", ".tmp")
        try {
            Files.write(temporary, bytes)
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
        return bytes
    }

    private fun decodeWearPositions(bytes: ByteArray): List<Int> {
        val buffer = ByteBuffer.wrap(bytes)

        fun byte(): Int = buffer.get().toInt() and 255
        var slots: List<Int>? = null
        while (true) {
            when (val opcode = byte()) {
                0 -> {
                    require(!buffer.hasRemaining()) { "Trailing wear-position defaults bytes" }
                    return checkNotNull(slots) { "Missing equipment-slot kinds in wear-position defaults" }
                }
                1 -> slots = List(byte()) { byte().also { require(it in 0..2) { "Unknown equipment-slot kind $it" } } }
                3, 4 -> byte()
                5, 6 -> repeat(byte()) { byte() }
                else -> error("Unknown wear-position defaults opcode $opcode")
            }
        }
    }

    private fun checksum(bytes: ByteArray): Int = CRC32().apply { update(bytes) }.value.toInt()

    private fun unpack(bytes: ByteArray): ByteArray {
        require(bytes.size >= 5)
        if (bytes[0].toInt() != 0) {
            require(bytes.size >= 9 && ByteBuffer.wrap(bytes).getInt(5) in 0..64 * 1024 * 1024) {
                "Excessive RS3 decompressed group size"
            }
        }
        return Unpooled.wrappedBuffer(bytes).use { compressed ->
            Js5Compression.uncompress(compressed).use { data ->
                ByteArray(data.readableBytes()).also { data.readBytes(it) }
            }
        }
    }

    private companion object {
        val logger = InlineLogger()
    }
}
