package net.rsprox.cache.rs3

import java.io.BufferedInputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer

public data class Rs3Js5ConnectionInfo(
    public val host: String,
    public val port: Int,
    public val revision: Int,
    public val token: String,
    public val subrevision: Int = 1,
    public val language: Int = 0,
) {
    // The token is a connection credential, not a diagnostic field.
    override fun toString(): String = "Rs3Js5ConnectionInfo($host:$port, revision=$revision.$subrevision)"
}

/** Bounded, single-connection JS5 pipeline, never called on the game event loop. */
internal class Rs3Js5Connection(
    private val info: Rs3Js5ConnectionInfo,
) : AutoCloseable {
    private var socket: Socket? = null
    private var input: DataInputStream? = null

    data class Request(
        val archive: Int,
        val group: Int,
    ) {
        init {
            require(archive in 0..255 && group >= 0)
        }
    }

    private fun connect(): Socket {
        val connection = Socket()
        try {
            connection.connect(InetSocketAddress(info.host, info.port), 10_000)
            connection.soTimeout = 15_000
            connection.tcpNoDelay = true
            val token = info.token.toByteArray(Charsets.ISO_8859_1)
            require(token.size <= 245 && 0.toByte() !in token) { "Invalid JS5 token length/content" }
            val output = DataOutputStream(connection.getOutputStream())
            output.writeByte(15)
            output.writeByte(10 + token.size)
            output.writeInt(info.revision)
            output.writeInt(info.subrevision)
            output.write(token)
            output.writeByte(0)
            output.writeByte(info.language)
            output.flush()
            val status = DataInputStream(connection.getInputStream()).readUnsignedByte()
            if (status != 0) throw IOException("RS3 JS5 handshake rejected: $status")
            // RS3 control and request records are padded to ten bytes.
            output.write(
                ByteBuffer
                    .allocate(10)
                    .put(6)
                    .put(0)
                    .put(0)
                    .put(5)
                    .putInt(info.revision)
                    .array(),
            )
            output.write(ByteBuffer.allocate(10).put(3).array())
            output.flush()
            socket = connection
            input = DataInputStream(BufferedInputStream(connection.getInputStream()))
            return connection
        } catch (failure: Exception) {
            connection.close()
            throw failure
        }
    }

    fun get(
        archive: Int,
        group: Int,
    ): ByteArray {
        var result: ByteArray? = null
        getAll(listOf(Request(archive, group))) { _, bytes -> result = bytes }
        return checkNotNull(result)
    }

    /** Responses may arrive out of order and large groups may interleave at block boundaries. */
    fun getAll(
        requests: List<Request>,
        accept: (Request, ByteArray) -> Unit,
    ) {
        val remaining = requests.toMutableSet()
        if (remaining.isEmpty()) return
        repeat(2) { attempt ->
            try {
                request(socket ?: connect(), remaining) { request, bytes ->
                    accept(request, bytes)
                    remaining.remove(request)
                }
                return
            } catch (failure: IOException) {
                close()
                if (attempt == 1) throw IOException("RS3 JS5 fetch failed: ${remaining.size} groups remaining", failure)
            }
        }
        error("Unreachable")
    }

    private fun request(
        connection: Socket,
        requests: Set<Request>,
        accept: (Request, ByteArray) -> Unit,
    ) {
        val output = connection.getOutputStream()
        val input = checkNotNull(input)
        // Copy before callbacks remove completed requests from the retry set.
        val queued = requests.toList().iterator()
        val pending = LinkedHashMap<Request, Response>()
        var allocated = 0L
        while (queued.hasNext() || pending.isNotEmpty()) {
            val batch = ByteBuffer.allocate(MAX_IN_FLIGHT * 10)
            while (queued.hasNext() && pending.size < MAX_IN_FLIGHT) {
                val request = queued.next()
                batch.put(1).put(request.archive.toByte()).putInt(request.group).putInt(0)
                pending[request] = Response()
            }
            if (batch.position() != 0) {
                output.write(batch.array(), 0, batch.position())
                output.flush()
            }
            if (pending.values.any { System.nanoTime() > it.deadline }) {
                throw IOException("RS3 JS5 group deadline exceeded")
            }
            val request = Request(input.readUnsignedByte(), input.readInt() and Int.MAX_VALUE)
            val response = pending[request] ?: throw IOException("Unexpected RS3 JS5 response $request")
            if (response.bytes == null) {
                val compression = input.readUnsignedByte()
                val length = input.readInt()
                if (compression !in 0..3 || length !in 0..MAX_GROUP_SIZE) {
                    throw IOException("Invalid RS3 JS5 container type/length: $compression/$length")
                }
                val size = length + if (compression == 0) 5 else 9
                if (allocated + size > MAX_BUFFERED_SIZE) throw IOException("Excessive RS3 JS5 buffered data")
                response.bytes = ByteArray(size)
                allocated += size
                ByteBuffer.wrap(response.bytes).put(compression.toByte()).putInt(length)
                response.position = 5
            }
            val bytes = checkNotNull(response.bytes)
            val count = minOf(bytes.size - response.position, BLOCK_SIZE - response.position % BLOCK_SIZE)
            input.readFully(bytes, response.position, count)
            response.position += count
            if (response.position == bytes.size) {
                accept(request, bytes)
                pending.remove(request)
                allocated -= bytes.size
            }
        }
    }

    override fun close() {
        socket?.close()
        socket = null
        input = null
    }

    private class Response {
        val deadline: Long = System.nanoTime() + 60_000_000_000L
        var bytes: ByteArray? = null
        var position: Int = 0
    }

    private companion object {
        const val BLOCK_SIZE = 100 * 1024 - 5
        const val MAX_GROUP_SIZE = 32 * 1024 * 1024
        const val MAX_BUFFERED_SIZE = 64 * 1024 * 1024
        const val MAX_IN_FLIGHT = 16
    }
}
