package net.rsprox.proxy.rs3.login

/**
 * Revision 950 login-success framing, not a game-packet decoder.
 * Native states: 90/96 result, 250/260/270 variable blocks, 130/136 second result,
 * 140 length and 150 success body; reconnect response 15 enters state 200 (g2 length and player table).
 * See RS3_950_LOGIN_ROUTING.md for evidence.
 */
public open class Rs3LoginSuccessFramer protected constructor(
    private val world: Boolean,
    private val onVariablesComplete: () -> Unit = {},
    private val retainVariables: Boolean = false,
    private val reconnect: Boolean = false,
) : Rs3LoginResponseFramer {
    private enum class State {
        RESULT,
        VARIABLES_LENGTH,
        VARIABLES,
        FINAL_RESULT,
        DATA_LENGTH,
        DATA,
        RECONNECT_LENGTH,
        RECONNECT_DATA,
    }

    final override var isDone: Boolean = false
        private set
    final override var isSuccessful: Boolean = false
        private set
    final override var initialCipherDraws: Int = 0
        private set

    public var loginData: ByteArray? = null
        private set

    public var reconnectData: ByteArray? = null
        private set

    /** Final two g8 values, shared by lobby/game success and the OSRS account identity contract. */
    public val userId: Long?
        get() = identityLong(16)

    public val userHash: Long?
        get() = identityLong(8)

    private fun identityLong(distanceFromEnd: Int): Long? {
        val data = loginData ?: return null
        if (data.size < 25 + initialCipherDraws) return null
        return java.nio.ByteBuffer
            .wrap(data, data.size - distanceFromEnd, Long.SIZE_BYTES)
            .long
    }

    public val variableBlocks: MutableList<ByteArray> = ArrayList()
    public var initializationTruncated: Boolean = false
        private set
    private var variableBytes = 0

    private var state = State.RESULT
    private var frame = ByteArray(1)
    private var received = 0

    override fun consume(chunk: ByteArray): ByteArray? {
        if (isDone) return if (isSuccessful) chunk else null
        var offset = 0
        while (offset < chunk.size) {
            val count = minOf(frame.size - received, chunk.size - offset)
            chunk.copyInto(frame, received, offset, offset + count)
            received += count
            offset += count
            if (received != frame.size) return null
            completeFrame()
            if (isDone) {
                return if (isSuccessful) chunk.copyOfRange(offset, chunk.size) else null
            }
        }
        return null
    }

    private fun completeFrame() {
        when (state) {
            State.RESULT, State.FINAL_RESULT -> {
                if (world && reconnect && state == State.RESULT && unsigned(0) == 15) {
                    next(State.RECONNECT_LENGTH, 2)
                    return
                }
                if (reconnect) {
                    isDone = true
                    frame = ByteArray(0)
                    return
                }
                if (unsigned(0) != 2) {
                    // Never start a game decoder on an unsuccessful/unsupported login response.
                    // The relay still forwards these bytes unchanged to the client.
                    isDone = true
                    frame = ByteArray(0)
                    return
                }
                if (world && state == State.RESULT) {
                    next(State.VARIABLES_LENGTH, 2)
                } else {
                    next(State.DATA_LENGTH, 1)
                }
            }
            State.VARIABLES_LENGTH -> {
                val length = (unsigned(0) shl 8) or unsigned(1)
                require(length > 0) { "World-login variable block is missing its completion flag" }
                next(State.VARIABLES, length)
            }
            State.VARIABLES -> {
                if (retainVariables && !initializationTruncated) {
                    variableBytes += frame.size
                    if (variableBytes > 16 * 1024 * 1024 || variableBlocks.size >= 65534) {
                        initializationTruncated = true
                        variableBlocks.clear()
                    } else {
                        variableBlocks += frame.copyOf()
                    }
                }
                // The block's first byte, not the TCP chunk boundary, determines completion.
                if (unsigned(0) == 1) {
                    onVariablesComplete()
                    next(State.FINAL_RESULT, 1)
                } else {
                    next(State.VARIABLES_LENGTH, 2)
                }
            }
            State.DATA_LENGTH -> {
                val length = unsigned(0)
                require(length > 0) { "Empty login-success body" }
                next(State.DATA, length)
            }
            State.DATA -> {
                // FUN_00285180 consumes four ISAAC values only when this flag equals one.
                initialCipherDraws = if (unsigned(0) == 1) 4 else 0
                val minimum = if (world) 9 else 1
                require(frame.size >= minimum + initialCipherDraws) { "Truncated login-success prefix" }
                loginData = frame
                frame = ByteArray(0)
                isSuccessful = true
                isDone = true
            }
            State.RECONNECT_LENGTH -> {
                val length = (unsigned(0) shl 8) or unsigned(1)
                require(length > 0) { "Empty reconnect initialization" }
                next(State.RECONNECT_DATA, length)
            }
            State.RECONNECT_DATA -> {
                reconnectData = frame
                frame = ByteArray(0)
                isSuccessful = true
                isDone = true
            }
        }
    }

    private fun next(
        state: State,
        length: Int,
    ) {
        this.state = state
        frame = ByteArray(length)
        received = 0
    }

    private fun unsigned(index: Int): Int = frame[index].toInt() and 0xFF
}
