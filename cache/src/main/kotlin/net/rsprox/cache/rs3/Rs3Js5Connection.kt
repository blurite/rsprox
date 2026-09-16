package net.rsprox.cache.rs3

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

/** Sequential, bounded JS5 transport for bootstrap downloads, never called on the game event loop. */
internal class Rs3Js5Connection(private val info: Rs3Js5ConnectionInfo) : AutoCloseable {
    private var socket: Socket? = null

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
            output.write(ByteBuffer.allocate(10).put(6).put(0).put(0).put(5).putInt(info.revision).array())
            output.write(ByteBuffer.allocate(10).put(3).array())
            output.flush()
            socket = connection
            return connection
        } catch (failure: Exception) {
            connection.close()
            throw failure
        }
    }

    fun get(archive: Int, group: Int): ByteArray {
        require(archive in 0..255 && group >= 0)
        repeat(2) { attempt ->
            try {
                return request(socket ?: connect(), archive, group)
            } catch (failure: IOException) {
                close()
                if (attempt == 1) throw IOException("RS3 JS5 fetch failed for $archive:$group", failure)
            }
        }
        error("Unreachable")
    }

    private fun request(connection: Socket, archive: Int, group: Int): ByteArray {
        val output = connection.getOutputStream()
        output.write(ByteBuffer.allocate(10).put(1).put(archive.toByte()).putInt(group).array())
        output.flush()
        val input = DataInputStream(connection.getInputStream())
        fun header() {
            val receivedArchive = input.readUnsignedByte()
            val receivedGroup = input.readInt() and Int.MAX_VALUE
            if (receivedArchive != archive || receivedGroup != group) {
                throw IOException("Unexpected RS3 JS5 response $receivedArchive:$receivedGroup")
            }
        }
        header()
        val compression = input.readUnsignedByte()
        val length = input.readInt()
        if (compression !in 0..3 || length !in 0..MAX_GROUP_SIZE) {
            throw IOException("Invalid RS3 JS5 container type/length: $compression/$length")
        }
        val bytes = ByteArray(length + if (compression == 0) 5 else 9)
        ByteBuffer.wrap(bytes).put(compression.toByte()).putInt(length)
        var position = 5
        val deadline = System.nanoTime() + 60_000_000_000L
        while (position < bytes.size) {
            if (System.nanoTime() > deadline) throw IOException("RS3 JS5 group deadline exceeded")
            if (position % BLOCK_SIZE == 0) header()
            val count = minOf(bytes.size - position, BLOCK_SIZE - position % BLOCK_SIZE)
            input.readFully(bytes, position, count)
            position += count
        }
        return bytes
    }

    override fun close() {
        socket?.close()
        socket = null
    }

    private companion object {
        const val BLOCK_SIZE = 100 * 1024 - 5
        const val MAX_GROUP_SIZE = 32 * 1024 * 1024
    }
}
