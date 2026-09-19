package net.rsprox.proxy.rs3.relay

import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipherPair
import net.rsprox.proxy.rs3.binary.Rs3BinaryRecorder
import net.rsprox.proxy.rs3.login.Rs3LoginSuccessFramer
import net.rsprox.proxy.rs3.login.Rs3WorldLoginResponseFramer
import net.rsprox.proxy.rs3.privacy.Rs3PacketSanitizer
import net.rsprox.proxy.rs3.protocol.Rs3ProtDecoder.ProtEntry

/** Independent framing/ciphers: UI filters cannot alter recording; privacy rules apply before storage. */
internal class Rs3ConnectionRecording(
    private val recorder: Rs3BinaryRecorder,
    private val connection: Rs3BinaryRecorder.Connection,
    private val revision: Int,
    private val servers: Map<Int, ProtEntry>,
    private val clients: Map<Int, ProtEntry>,
    huffman: HuffmanCodec,
) {
    private val sanitizer = Rs3PacketSanitizer(huffman)
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
        // Note(revision): Verify/update Rs3PacketSanitizer and the live GUI policy before enabling another revision.
        require(major == revision && major == 950) { "Unsupported RS3 recording revision" }
        require(servers.keys.none { it in 0xFC..0xFF }) { "Recording opcode collision" }
        this.minor = minor
        serverStream?.close()
        clientStream?.close()
        ciphers = pair
        serverStream =
            Rs3PacketStream(servers, { pair.decodeCipher }, true) { entry, payload ->
                // Normalize payload ISAAC before redaction, advancing the original cipher exactly once.
                sanitizer.normalizeServerPayload(entry.name, payload) { pair.decodeCipher }
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
            val payload = sanitizer.sanitize(server, it.entry.name, it.payload)
            if (payload != null) {
                recorder.packet(connection, server, it.opcode, it.entry.length, payload)
            }
        }
    }

    fun reconnect(payload: ByteArray) =
        safely {
            check(successful) { "Reconnect has no previous recording" }
            recorder.packet(connection, true, 0xFF, -2, payload)
        }

    /** A broken socket may end halfway through a packet. Only complete packets were recorded. */
    fun suspend() {
        serverStream?.close()
        clientStream?.close()
        serverStream = null
        clientStream = null
        ciphers = null
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
