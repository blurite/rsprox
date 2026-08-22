package net.rsprox.proxy.rs3.protocol

import net.rsprot.crypto.cipher.StreamCipher

public class Rs3ProtDecoder(
    private val table: Map<Int, ProtEntry>,
    private val extraCipherDraws: Map<Int, Int> = emptyMap(),
    private val supportsExtendedOpcodes: Boolean = true,
    private val cipher: () -> StreamCipher?,
    private val onPacket: (opcode: Int, payload: ByteArray) -> Unit,
) {
    public data class ProtEntry(val length: Int, val name: String)

    private enum class State {
        AWAITING_OPCODE,
        AWAITING_OPCODE_BYTE2,
        AWAITING_LENGTH_BYTE,
        AWAITING_LENGTH_SHORT,
        AWAITING_PAYLOAD,
    }

    private var state = State.AWAITING_OPCODE
    private var buffer = ByteArray(0)
    private var currentOpcode = -1
    private var currentEntry: ProtEntry? = null
    private var currentLength = -1
    private var pendingOpcodeByte1Decrypted = -1

    public fun accept(chunk: ByteArray) {
        buffer += chunk
        process()
    }

    private fun resolveOpcode(): Boolean {
        val entry = table[currentOpcode]
        if (entry == null) {
            buffer = ByteArray(0)
            state = State.AWAITING_OPCODE
            return false
        }
        currentEntry = entry
        state =
            when {
                entry.length >= 0 -> {
                    currentLength = entry.length
                    State.AWAITING_PAYLOAD
                }
                entry.length == -1 -> State.AWAITING_LENGTH_BYTE
                entry.length == -2 -> State.AWAITING_LENGTH_SHORT
                else -> {
                    buffer = ByteArray(0)
                    state = State.AWAITING_OPCODE
                    return false
                }
            }
        return true
    }

    private fun process() {
        while (true) {
            when (state) {
                State.AWAITING_OPCODE -> {
                    val activeCipher = cipher() ?: run {
                        buffer = ByteArray(0)
                        return
                    }
                    if (buffer.isEmpty()) return
                    val raw = buffer[0].toInt() and 0xFF
                    val decrypted = (raw - activeCipher.nextInt()) and 0xFF
                    buffer = buffer.copyOfRange(1, buffer.size)

                    if (supportsExtendedOpcodes && decrypted >= 128) {
                        pendingOpcodeByte1Decrypted = decrypted
                        state = State.AWAITING_OPCODE_BYTE2
                    } else {
                        currentOpcode = decrypted
                        if (!resolveOpcode()) return
                    }
                }

                State.AWAITING_OPCODE_BYTE2 -> {
                    val activeCipher = cipher() ?: run {
                        buffer = ByteArray(0)
                        return
                    }
                    if (buffer.isEmpty()) return
                    val raw2 = buffer[0].toInt() and 0xFF
                    val decrypted2 = (raw2 - activeCipher.nextInt()) and 0xFF
                    buffer = buffer.copyOfRange(1, buffer.size)
                    currentOpcode = decrypted2
                    if (!resolveOpcode()) return
                }

                State.AWAITING_LENGTH_BYTE -> {
                    if (buffer.isEmpty()) return
                    currentLength = buffer[0].toInt() and 0xFF
                    buffer = buffer.copyOfRange(1, buffer.size)
                    state = State.AWAITING_PAYLOAD
                }

                State.AWAITING_LENGTH_SHORT -> {
                    if (buffer.size < 2) return
                    currentLength = ((buffer[0].toInt() and 0xFF) shl 8) or (buffer[1].toInt() and 0xFF)
                    buffer = buffer.copyOfRange(2, buffer.size)
                    state = State.AWAITING_PAYLOAD
                }

                State.AWAITING_PAYLOAD -> {
                    if (currentLength > MAX_PLAUSIBLE_PAYLOAD) {
                        buffer = ByteArray(0)
                        state = State.AWAITING_OPCODE
                        return
                    }
                    if (buffer.size < currentLength) return
                    val payload = buffer.copyOfRange(0, currentLength)
                    buffer = buffer.copyOfRange(currentLength, buffer.size)

                    try {
                        onPacket(currentOpcode, payload)
                    } catch (_: Exception) {
                    }

                    val extraDraws = extraCipherDraws[currentOpcode] ?: 0
                    if (extraDraws > 0) {
                        val activeCipher = cipher()
                        repeat(extraDraws) { activeCipher?.nextInt() }
                    }

                    state = State.AWAITING_OPCODE
                }
            }
        }
    }

    private companion object {
        private const val MAX_PLAUSIBLE_PAYLOAD = 20_000
    }
}
