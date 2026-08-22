package net.rsprox.proxy.rs3.login

public class Rs3ServerLoginResponseFramer : Rs3LoginResponseFramer {
    private enum class State {
        AWAITING_RESULT_BYTE,
        AWAITING_LENGTH_BYTE,
        AWAITING_DATA,
        DONE,
        ABANDONED,
    }

    override var isDone: Boolean = false
        private set

    private var state = State.AWAITING_RESULT_BYTE
    private var resultCode = -1
    private var dataLength = 0
    private var buffer = ByteArray(0)

    override fun consume(chunk: ByteArray): ByteArray? {
        buffer += chunk

        if (state == State.AWAITING_RESULT_BYTE) {
            if (buffer.isEmpty()) return null
            resultCode = buffer[0].toInt() and 0xFF
            buffer = buffer.copyOfRange(1, buffer.size)

            if (resultCode != LOGIN_SUCCESS) {
                state = State.ABANDONED
                isDone = true
                return null
            }
            state = State.AWAITING_LENGTH_BYTE
        }

        if (state == State.ABANDONED) return null

        if (state == State.AWAITING_LENGTH_BYTE) {
            if (buffer.isEmpty()) return null
            dataLength = buffer[0].toInt() and 0xFF
            buffer = buffer.copyOfRange(1, buffer.size)
            state = State.AWAITING_DATA
        }

        if (state == State.AWAITING_DATA) {
            if (buffer.size < dataLength) return null
            val leftover = buffer.copyOfRange(dataLength, buffer.size)
            state = State.DONE
            isDone = true
            return leftover
        }

        return null
    }

    private companion object {
        private const val LOGIN_SUCCESS = 2
    }
}
