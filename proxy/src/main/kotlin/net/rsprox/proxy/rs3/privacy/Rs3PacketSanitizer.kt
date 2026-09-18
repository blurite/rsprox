package net.rsprox.proxy.rs3.privacy

import io.netty.buffer.Unpooled
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.compression.HuffmanCodec
import net.rsprot.crypto.cipher.StreamCipher
import java.util.zip.CRC32

/** Revision-950 privacy policy for recording and GUI copies, after payload ISAAC normalization. */
internal class Rs3PacketSanitizer(
    private val huffman: HuffmanCodec,
) {
    /** Mutates an observation-owned copy only; consumes the original payload's cipher draws. */
    fun normalizeServerPayload(
        name: String,
        payload: ByteArray,
        cipher: () -> StreamCipher,
    ) {
        // Note(revision): Recheck which URL payload bytes use ISAAC and the number of cipher draws.
        val start =
            when (name) {
                "URL_OPEN" -> 1
                "SOCIAL_NETWORK_LOGOUT" -> 0
                else -> return
            }
        val activeCipher = cipher()
        for (index in start until payload.size) {
            payload[index] = (payload[index].toInt() - activeCipher.nextInt()).toByte()
        }
    }

    /** Null omits sensitive actions or encrypted account data that cannot be safely rewritten. */
    fun sanitize(
        server: Boolean,
        name: String,
        payload: ByteArray,
    ): ByteArray? {
        if (!server && name in OMITTED_CLIENT_PACKETS) return null
        if (name !in (if (server) SERVER_PACKETS else CLIENT_PACKETS)) return payload

        // Never mutate transport buffers, live-decoder inputs, or the caller's original packet.
        val input = Cursor(payload.copyOf())
        var result = input.bytes
        try {
            if (!server && name in INTERFACE_ACTION_PACKETS) {
                return sanitizeInterfaceAction(name, input.bytes)
            }
            if (server) {
                // Note(revision): Verify these layouts, field offsets and buffer methods against the new server decoders.
                when (name) {
                    "MESSAGE_PRIVATE" -> {
                        val alternate = input.g1()
                        input.string()
                        if (alternate == 1) input.string()
                        input.skip(6) // World, message counter and player type.
                        result = privateMessage(input)
                    }
                    "MESSAGE_PRIVATE_ECHO" -> {
                        input.string()
                        result = privateMessage(input)
                    }
                    "URL_OPEN" -> {
                        val mode = input.g1()
                        input.string(url = true)
                        if (mode == 1) input.string(url = true)
                    }
                    "SOCIAL_NETWORK_LOGOUT" -> input.string(url = true)
                    "UPDATE_SITESETTINGS" -> input.string(mask = true)
                    "UPDATE_UID192" -> {
                        input.skip(28)
                        result.fill(0, 0, 24)
                        val crc = CRC32().apply { update(result, 0, 24) }.value
                        repeat(4) { result[24 + it] = (crc ushr (24 - it * 8)).toByte() }
                    }
                    "UPDATE_DOB" -> {
                        input.skip(4)
                        // Signed 24-bit -1 (unknown), retaining the verification flag.
                        result.fill((-1).toByte(), 0, 3)
                    }
                    "UPDATE_FRIENDLIST" -> {
                        while (input.remaining > 0) {
                            input.skip(1)
                            input.string()
                            input.string()
                            val world = input.g2()
                            input.skip(2)
                            if (world != 0) {
                                input.string()
                                input.skip(5)
                            }
                            input.string(mask = true)
                        }
                    }
                    "UPDATE_IGNORELIST" -> {
                        while (input.remaining > 0) {
                            input.skip(1)
                            input.string()
                            input.string()
                            input.string(mask = true)
                        }
                    }
                }
            } else {
                // Note(revision): Verify these layouts and buffer methods against the new client decoders.
                when (name) {
                    "MESSAGE_PRIVATE" -> {
                        input.string()
                        result = privateMessage(input)
                    }
                    "EVENT_KEYBOARD" -> {
                        // Note(revision): Recheck the key byte's position/encoding and event size before writing zero.
                        require(input.remaining > 0 && input.remaining % 4 == 0)
                        while (input.remaining > 0) {
                            result[input.position] = 0
                            input.skip(4)
                        }
                    }
                    "ADD_NEW_EMAIL_ADDRESS" -> {
                        input.string(mask = true)
                        input.skip(1)
                    }
                    "CHANGE_EMAIL_ADDRESS" -> {
                        input.string(mask = true)
                        input.string(mask = true)
                    }
                    "SEND_EMAIL_VALIDATION_CODE" -> input.string(mask = true)
                    "FRIEND_SETNOTES" -> {
                        input.string()
                        input.string(mask = true)
                    }
                    "IGNORE_SETNOTES" -> {
                        input.string(mask = true)
                        input.string()
                    }
                }
            }
            require(input.remaining == 0)
            return result
        } catch (_: Exception) {
            // No original payload, decoded string or nested codec exception may leak through error logs.
            // The recorder fails closed; live forwarding is independent and continues unchanged.
            error("Unable to sanitize RS3 packet: server=$server, name=$name")
        }
    }

    private fun sanitizeInterfaceAction(name: String, payload: ByteArray): ByteArray? {
        val buffer = Unpooled.wrappedBuffer(payload)
        return try {
            val input = buffer.toJagByteBuf()
            // Note(revision): Recheck both action layouts, lengths and alt methods; a wrong id can bypass PIN redaction.
            val combinedId =
                if (name == "RESUME_PAUSEBUTTON") {
                    require(payload.size == 6)
                    input.g4().also { input.g2Alt2() }
                } else {
                    require(payload.size == 9)
                    input.g3() // Object id.
                    input.g4Alt3().also { input.g2() } // Component id and slot.
                }
            // Digits are encoded by component identity. Cover every component/op, including
            // cancel/help, so shuffled or newly added buttons cannot bypass the policy.
            if ((combinedId ushr 16) in BANK_PIN_INTERFACES) null else payload
        } finally {
            buffer.release()
        }
    }

    private fun privateMessage(input: Cursor): ByteArray {
        val start = input.position
        val buffer = Unpooled.wrappedBuffer(input.bytes, start, input.remaining)
        val length =
            try {
                huffman.decode(buffer).length
            } finally {
                buffer.release()
            }
        input.skip(input.remaining)
        val output = Unpooled.buffer()
        return try {
            output.writeBytes(input.bytes, 0, start)
            huffman.encode(output, "*".repeat(length))
            ByteArray(output.readableBytes()).also { output.readBytes(it) }
        } finally {
            output.release()
        }
    }

    private class Cursor(val bytes: ByteArray) {
        var position = 0
            private set
        val remaining: Int
            get() = bytes.size - position

        fun skip(count: Int) {
            require(count in 0..remaining)
            position += count
        }

        fun g1(): Int {
            require(remaining > 0)
            return bytes[position++].toInt() and 255
        }

        fun g2(): Int = (g1() shl 8) or g1()

        fun string(mask: Boolean = false, url: Boolean = false) {
            val start = position
            while (g1() != 0) {
                // Preserve native byte representation and terminators, including non-ASCII names.
            }
            val end = position - 1
            if (mask || url) {
                val preserved =
                    if (url) {
                        val value = String(bytes, start, end - start, Charsets.ISO_8859_1)
                        URL_ORIGIN.find(value)?.value?.length ?: 0
                    } else {
                        0
                    }
                bytes.fill('*'.code.toByte(), start + preserved, end)
            }
        }
    }

    private companion object {
        // Preserve only an ordinary HTTP(S) origin. Paths, queries and fragments can carry tokens.
        // Userinfo, malformed URLs and other schemes are entirely masked, never passed through.
        val URL_ORIGIN = Regex("^https?://[a-z0-9.-]+(?::[0-9]+)?(?:[/?#]|$)", RegexOption.IGNORE_CASE)
        // Note(revision): Review protected/omitted packet names below, including new variants carrying sensitive fields.
        val OMITTED_CLIENT_PACKETS = setOf("CREATE_ACCOUNT", "CREATE_CHECK_EMAIL")
        // Note(revision): Verify both PIN interface ids in the cache; component ids are deliberately not restricted.
        val BANK_PIN_INTERFACES = setOf(13, 759) // bankpin_main, bankpin_numbers.
        // Note(revision): Verify the PIN submission packet types; new action variants must be covered here too.
        val INTERFACE_ACTION_PACKETS =
            (1..10).map { "IF_BUTTON${it}_V2" }.toSet() + "RESUME_PAUSEBUTTON"
        val SERVER_PACKETS =
            setOf(
                "MESSAGE_PRIVATE",
                "MESSAGE_PRIVATE_ECHO",
                "URL_OPEN",
                "SOCIAL_NETWORK_LOGOUT",
                "UPDATE_SITESETTINGS",
                "UPDATE_UID192",
                "UPDATE_DOB",
                "UPDATE_FRIENDLIST",
                "UPDATE_IGNORELIST",
            )
        val CLIENT_PACKETS =
            setOf(
                "MESSAGE_PRIVATE",
                "EVENT_KEYBOARD",
                "ADD_NEW_EMAIL_ADDRESS",
                "CHANGE_EMAIL_ADDRESS",
                "SEND_EMAIL_VALIDATION_CODE",
                "FRIEND_SETNOTES",
                "IGNORE_SETNOTES",
            ) + INTERFACE_ACTION_PACKETS
    }
}
