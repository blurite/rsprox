package net.rsprox.proxy.rs3.relay

import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.proxy.rs3.binary.Rs3BinaryRecorder
import net.rsprox.proxy.rs3.login.Rs3LoginSuccessFramer
import net.rsprox.proxy.rs3.login.Rs3WorldLoginResponseFramer
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder.ProtEntry

/** Independent strict framing/ciphers: decoder errors and UI filters cannot remove recorded packets. */
internal class Rs3ConnectionRecording(
    private val recorder: Rs3BinaryRecorder,
    private val connection: Rs3BinaryRecorder.Connection,
    private val revision: Int,
    private val servers: Map<Int, ProtEntry>,
    private val clients: Map<Int, ProtEntry>,
) {
    private var ciphers: StreamCipherPair? = null
    private var serverStream: Rs3PacketStream? = null
    private var clientStream: Rs3PacketStream? = null
    private var minor = 0
    private var successful = false
    private var failed = false
    private var closed = false

    fun begin(
        pair: StreamCipherPair,
        major: Int,
        minor: Int,
    ) = safely {
        require(major == revision && major == 950) { "Unsupported RS3 recording revision" }
        require(servers.keys.none { it in 0xFC..0xFF }) { "Recording opcode collision" }
        this.minor = minor
        ciphers = pair
        serverStream =
            Rs3PacketStream(servers, { pair.decodeCipher }, true) { entry, payload ->
                // Preserve original byte values, not re-encoded strings. No cipher seeds enter the file.
                val start =
                    when (entry.name) {
                        "URL_OPEN" -> 1
                        "SOCIAL_NETWORK_LOGOUT" -> 0
                        else -> payload.size
                    }
                for (index in start until payload.size) {
                    payload[index] = (payload[index].toInt() - pair.decodeCipher.nextInt()).toByte()
                }
            }
        clientStream = Rs3PacketStream(clients, { pair.encoderCipher }, false)
    }

    fun login(framer: Rs3LoginSuccessFramer) =
        safely {
            check(framer.isSuccessful && !successful)
            check(!framer.initializationTruncated) { "Login initialization exceeded recording bounds" }
            repeat(framer.initialCipherDraws) { checkNotNull(ciphers).decodeCipher.nextInt() }
            recorder.login(
                connection,
                minor,
                (framer as? Rs3WorldLoginResponseFramer)?.ownIndex ?: -1,
                checkNotNull(framer.loginData),
                framer.variableBlocks.toList(),
            )
            framer.variableBlocks.clear()
            successful = true
        }

    fun accept(
        server: Boolean,
        bytes: ByteArray,
    ) = safely {
        check(successful) { "Game packets arrived before successful login" }
        val stream = checkNotNull(if (server) serverStream else clientStream)
        stream.accept(bytes) {
            recorder.packet(connection, server, it.opcode, it.entry.length, it.payload)
        }
    }

    fun close() {
        if (closed) return
        if (successful) {
            safely {
                serverStream?.finishInput()
                clientStream?.finishInput()
            }
        }
        closed = true
        serverStream?.close()
        clientStream?.close()
        recorder.close(connection, failed)
    }

    private inline fun safely(action: () -> Unit) {
        if (closed || failed) return
        try {
            action()
        } catch (error: Exception) {
            failed = true
            recorder.fail(error)
        }
    }
}
