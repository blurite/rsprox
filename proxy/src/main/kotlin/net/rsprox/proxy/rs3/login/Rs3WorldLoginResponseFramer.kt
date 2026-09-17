package net.rsprox.proxy.rs3.login

public class Rs3WorldLoginResponseFramer(
    onVariablesComplete: () -> Unit = {},
) : Rs3LoginSuccessFramer(world = true, onVariablesComplete) {
    public val ownIndex: Int?
        get() {
            val data = loginData ?: return null
            val offset = 7 + initialCipherDraws
            return ((data[offset].toInt() and 0xFF) shl 8) or (data[offset + 1].toInt() and 0xFF)
        }
}
