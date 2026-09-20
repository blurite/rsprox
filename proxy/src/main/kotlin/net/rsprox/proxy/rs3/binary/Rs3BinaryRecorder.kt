package net.rsprox.proxy.rs3.binary

import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.ByteBufAllocator
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.config.BINARY_PATH
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/** One launched client; independent connection identities; disk work never runs on Netty. */
internal class Rs3BinaryRecorder(
    private val revision: Int,
    private val masterIndex: ByteArray,
    private val directory: Path = BINARY_PATH.resolve("RuneScape 3"),
    private val clientName: String = "RS3 Native",
) {
    data class WorldMetadata(
        val flags: Int,
        val location: Int,
        val activity: String,
    )

    class Connection(
        val world: Boolean,
        val id: Int,
        val host: String,
        val port: Int,
    ) {
        internal var startNanos = 0L
        internal var lastMillis = 0L
        internal var prefix: ByteArrayOutputStream? = null
        internal var accountHash: ByteArray? = null
        internal var file: RecordingFile? = null
        internal var closed = false
    }

    private data class Lobby(
        val bytes: ByteArray,
        val duration: Long,
        val lastMillis: Long,
        val id: Int,
        val accountHash: ByteArray,
    )

    internal class RecordingFile(
        val path: Path,
        val channel: FileChannel,
    ) {
        private var goodLength = 0L
        private var lastFlush = System.nanoTime()

        fun append(bytes: ByteArray) {
            val buffer = ByteBuffer.wrap(bytes)
            while (buffer.hasRemaining()) channel.write(buffer)
            goodLength = channel.position()
            if (System.nanoTime() - lastFlush >= 5_000_000_000L) {
                channel.force(false)
                lastFlush = System.nanoTime()
            }
        }

        fun finish(incomplete: Boolean = false) {
            if (!channel.isOpen) return
            try {
                channel.truncate(goodLength)
                channel.force(true)
            } finally {
                channel.close()
            }
            val suffix = if (incomplete) ".incomplete.bin" else ".bin"
            val target = path.resolveSibling(path.fileName.toString().removeSuffix(".bin.part") + suffix)
            try {
                Files.move(path, target, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
                Files.move(path, target)
            }
            logger.info { "RS3 binary saved: $target" }
        }

        companion object {
            fun create(
                directory: Path,
                fileName: String,
            ): RecordingFile {
                val stem = fileName.removeSuffix(".bin")
                var number = 1
                while (true) {
                    val name = if (number == 1) stem else "$stem-$number"
                    number++
                    val complete = directory.resolve("$name.bin")
                    val incomplete = directory.resolve("$name.incomplete.bin")
                    if (Files.exists(complete) || Files.exists(incomplete)) continue
                    val path = directory.resolve("$name.bin.part")
                    val channel =
                        try {
                            FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
                        } catch (_: FileAlreadyExistsException) {
                            continue
                        }
                    // Another writer may have finalized this name between the check and our reservation.
                    if (Files.exists(complete) || Files.exists(incomplete)) {
                        channel.close()
                        Files.delete(path) // Only our newly-created, empty reservation.
                        continue
                    }
                    return RecordingFile(path, channel)
                }
            }
        }
    }

    private val writer =
        Executors.newSingleThreadExecutor { task ->
            Thread(task, "rs3-binary-writer").apply { isDaemon = true }
        }
    private val queuedBytes = AtomicLong()
    private val connections = HashSet<Connection>()
    private var lobby: Connection? = null
    private var frozenLobby: Lobby? = null
    private var worlds: Map<Int, WorldMetadata> = emptyMap()

    @Volatile private var disabled = false
    private var stopped = false
    private var overloaded = false
    private var shutdownFuture: CompletableFuture<Unit>? = null

    fun connection(
        world: Boolean,
        id: Int,
        host: String,
        port: Int,
    ): Connection = Connection(world, id, host, port)

    /** Full definitions replace the snapshot; population-only replies must leave it unchanged. */
    fun worldDefinitions(definitions: Map<Int, WorldMetadata>) {
        val snapshot = definitions.toMap()
        submit(0) { worlds = snapshot }
    }

    fun login(
        connection: Connection,
        minor: Int,
        ownIndex: Int,
        body: ByteArray,
        variables: List<ByteArray>,
        nowNanos: Long = System.nanoTime(),
        nowMillis: Long = System.currentTimeMillis(),
    ) {
        require(connection.id in 0..65535 && connection.port in 1..65535) { "Unidentified RS3 recording endpoint" }
        val sanitized = Rs3RecordingEncoding.sanitizeSuccess(body)
        // The final two big-endian g8 values are the same account identity pair used by OSRS UserUid.
        // Only their SHA-256 enters the recording; the raw values remain sanitized in initialization.
        val identityDigest = MessageDigest.getInstance("SHA-256")
        identityDigest.update(body, body.size - 2 * Long.SIZE_BYTES, 2 * Long.SIZE_BYTES)
        val accountHash = identityDigest.digest()
        require(variables.size <= 65534)
        if (connection.world) {
            require(variables.isNotEmpty() && variables.all { it.size in 1..65535 }) { "Missing game-login variables" }
            require(variables.last()[0] == 1.toByte() && variables.dropLast(1).none { it[0] == 1.toByte() }) {
                "Invalid game-login variable completion"
            }
        }
        require(variables.sumOf { it.size.toLong() } <= Rs3RecordingEncoding.MAX_INITIALIZATION)
        submit(sanitized.size + variables.sumOf { it.size }) {
            check(!connection.closed && connection.startNanos == 0L) { "Duplicate RS3 recording login" }
            connections += connection
            connection.startNanos = nowNanos
            connection.accountHash = accountHash
            if (!connection.world) {
                connection.prefix =
                    ByteArrayOutputStream().apply {
                        write(Rs3RecordingEncoding.initialization(false, 0, sanitized))
                    }
                // A new real lobby supersedes the old baseline; existing game writers stay independent.
                lobby?.prefix = null
                lobby = connection
                frozenLobby = null
            } else {
                val copied = frozenLobby != null
                val prefix =
                    frozenLobby ?: run {
                        val source = checkNotNull(lobby) { "Game login has no captured lobby baseline" }
                        Lobby(
                            checkNotNull(source.prefix).toByteArray(),
                            elapsed(source, nowNanos),
                            source.lastMillis,
                            source.id,
                            checkNotNull(source.accountHash),
                        ).also {
                            frozenLobby = it
                            source.prefix = null // Freeze before any late packets on the old lobby.
                        }
                    }
                check(
                    prefix.accountHash.contentEquals(accountHash),
                ) { "Game login account differs from captured lobby" }
                require(ownIndex in 0..65534)
                val world = worlds[connection.id]
                if (world == null) {
                    logger.warn {
                        "No captured RS3 world definitions for ${connection.id}; header metadata remains unknown"
                    }
                }
                require(world == null || world.location in 0..255) { "RS3 country ID exceeds binary header byte" }
                val header =
                    BinaryHeader(
                        BinaryHeader.HEADER_VERSION,
                        revision,
                        minor,
                        255,
                        255,
                        nowMillis - prefix.duration,
                        connection.id,
                        world?.flags ?: 0,
                        world?.location ?: 0,
                        connection.host,
                        world?.activity ?: "",
                        ownIndex,
                        accountHash,
                        clientName,
                        masterIndex,
                    )
                Files.createDirectories(directory)
                val file = RecordingFile.create(directory, header.fileName(nowMillis))
                connection.file = file
                val encoded = header.encode(ByteBufAllocator.DEFAULT).buffer
                try {
                    file.append(ByteArray(encoded.readableBytes()).also { encoded.readBytes(it) })
                } finally {
                    encoded.release()
                }
                file.append(prefix.bytes)
                file.append(Rs3RecordingEncoding.initialization(true, 0, sanitized))
                variables.forEachIndexed { index, block ->
                    file.append(Rs3RecordingEncoding.initialization(true, index + 1, block))
                }
                val marker =
                    Rs3RecordingEncoding.bytes {
                        it.writeByte(Rs3RecordingEncoding.VERSION)
                        it.writeShort(connection.id)
                        it.writeShort(connection.port)
                        it.writeShort(prefix.id)
                        it.writeShort(ownIndex)
                        it.writeLong(nowMillis)
                        it.writeLong(prefix.duration)
                        it.writeBoolean(copied)
                        it.writeShort(variables.size)
                        it.writeInt(variables.sumOf { block -> block.size })
                    }
                file.append(
                    Rs3RecordingEncoding.record(
                        true,
                        Rs3RecordingProt.LOBBY_TRANSFER.opcode,
                        -2,
                        marker,
                        (prefix.duration - prefix.lastMillis).coerceAtLeast(0),
                    ),
                )
                file.channel.force(true)
                logger.info { "RS3 binary recording started: ${file.path}" }
            }
        }
    }

    fun packet(
        connection: Connection,
        server: Boolean,
        opcode: Int,
        size: Int,
        payload: ByteArray,
        nowNanos: Long = System.nanoTime(),
    ) {
        submit(payload.size + 16) {
            if (connection.closed) return@submit
            val prefix = connection.prefix
            val file = connection.file
            if (prefix == null && file == null) return@submit
            val millis = elapsed(connection, nowNanos).coerceAtLeast(connection.lastMillis)
            val record = Rs3RecordingEncoding.record(server, opcode, size, payload, millis - connection.lastMillis)
            connection.lastMillis = millis
            if (file != null) {
                file.append(record)
            } else {
                checkNotNull(prefix)
                require(prefix.size().toLong() + record.size <= MAX_BUFFER) { "RS3 lobby capture exceeds 64 MiB" }
                prefix.write(record)
            }
        }
    }

    fun close(
        connection: Connection,
        incomplete: Boolean = false,
    ) {
        submit(0) {
            if (!connection.closed) {
                connection.closed = true
                connection.file?.finish(incomplete)
                connections -= connection
                // A successful lobby prefix survives its socket closing.
            }
        }
    }

    fun fail(cause: Throwable) {
        submit(0) { throw IllegalStateException("RS3 recording cannot continue losslessly", cause) }
    }

    @Synchronized
    fun shutdown(): CompletableFuture<Unit> {
        shutdownFuture?.let { return it }
        stopped = true
        val future = CompletableFuture<Unit>()
        shutdownFuture = future
        writer.execute {
            try {
                connections.forEach { it.file?.finish(disabled) }
                connections.clear()
                lobby = null
                frozenLobby = null
                future.complete(Unit)
            } catch (error: Exception) {
                future.completeExceptionally(error)
            }
        }
        writer.shutdown()
        return future
    }

    @Synchronized
    private fun submit(
        bytes: Int,
        action: () -> Unit,
    ) {
        if (stopped || disabled || overloaded) return
        if (queuedBytes.addAndGet(bytes.toLong()) > MAX_BUFFER) {
            queuedBytes.addAndGet(-bytes.toLong())
            overloaded = true
            writer.execute { disable(IllegalStateException("RS3 recording queue exceeds 64 MiB")) }
            return
        }
        writer.execute {
            try {
                if (!disabled) action()
            } catch (error: Exception) {
                disable(error)
            } finally {
                queuedBytes.addAndGet(-bytes.toLong())
            }
        }
    }

    private fun disable(error: Exception) {
        disabled = true
        logger.error(error) { "RS3 binary recording stopped; gameplay continues. Capture is incomplete." }
        connections.forEach {
            try {
                it.file?.finish(true)
            } catch (closeError: Exception) {
                logger.error(closeError) { "Could not finalize partial RS3 recording" }
            }
        }
    }

    private fun elapsed(
        connection: Connection,
        now: Long,
    ): Long = ((now - connection.startNanos) / 1_000_000).coerceAtLeast(0)

    private companion object {
        const val MAX_BUFFER = 64L * 1024 * 1024
        val logger = InlineLogger()
    }
}
