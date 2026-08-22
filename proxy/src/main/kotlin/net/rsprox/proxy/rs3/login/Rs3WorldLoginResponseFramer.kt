package net.rsprox.proxy.rs3.login

public class Rs3WorldLoginResponseFramer : Rs3LoginResponseFramer {
    private enum class State {
        AWAITING_LEADING_RESULT_BYTE,
        AWAITING_VARCS_LEN_BYTES,
        AWAITING_VARCS_DATA,
        AWAITING_RESULT_BYTE,
        AWAITING_LOGINDATA_LEN_BYTE,
        AWAITING_LOGINDATA_DATA,
        DONE,
        ABANDONED,
    }

    override var isDone: Boolean = false
        private set

    private var state = State.AWAITING_LEADING_RESULT_BYTE
    private var leadingResultCode = -1
    private var varcsLen = 0
    private var resultCode = -1
    private var loginDataLen = 0
    private var buffer = ByteArray(0)

    public var rawVarcs: ByteArray? = null
        private set
    public var loginData: ByteArray? = null
        private set

    public val ownIndex: Int?
        get() {
            val data = loginData ?: return null
            if (data.size < 9) return null
            return ((data[7].toInt() and 0xFF) shl 8) or (data[8].toInt() and 0xFF)
        }

    override fun consume(chunk: ByteArray): ByteArray? {
        buffer += chunk

        if (state == State.AWAITING_LEADING_RESULT_BYTE) {
            if (buffer.isEmpty()) return null
            leadingResultCode = buffer[0].toInt() and 0xFF
            buffer = buffer.copyOfRange(1, buffer.size)

            if (leadingResultCode != LOGIN_SUCCESS) {
                state = State.ABANDONED
                isDone = true
                return null
            }
            state = State.AWAITING_VARCS_LEN_BYTES
        }

        if (state == State.ABANDONED) return null

        if (state == State.AWAITING_VARCS_LEN_BYTES) {
            if (buffer.size < 2) return null
            varcsLen = ((buffer[0].toInt() and 0xFF) shl 8) or (buffer[1].toInt() and 0xFF)
            buffer = buffer.copyOfRange(2, buffer.size)
            state = State.AWAITING_VARCS_DATA
        }

        if (state == State.AWAITING_VARCS_DATA) {
            if (buffer.size < varcsLen) return null
            rawVarcs = buffer.copyOfRange(0, varcsLen)
            buffer = buffer.copyOfRange(varcsLen, buffer.size)
            state = State.AWAITING_RESULT_BYTE
        }

        if (state == State.AWAITING_RESULT_BYTE) {
            if (buffer.isEmpty()) return null
            resultCode = buffer[0].toInt() and 0xFF
            buffer = buffer.copyOfRange(1, buffer.size)

            if (resultCode != LOGIN_SUCCESS) {
                state = State.ABANDONED
                isDone = true
                return null
            }
            state = State.AWAITING_LOGINDATA_LEN_BYTE
        }

        if (state == State.ABANDONED) return null

        if (state == State.AWAITING_LOGINDATA_LEN_BYTE) {
            if (buffer.isEmpty()) return null
            loginDataLen = buffer[0].toInt() and 0xFF
            buffer = buffer.copyOfRange(1, buffer.size)
            state = State.AWAITING_LOGINDATA_DATA
        }

        if (state == State.AWAITING_LOGINDATA_DATA) {
            if (buffer.size < loginDataLen) return null
            loginData = buffer.copyOfRange(0, loginDataLen)
            val leftover = buffer.copyOfRange(loginDataLen, buffer.size)

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
