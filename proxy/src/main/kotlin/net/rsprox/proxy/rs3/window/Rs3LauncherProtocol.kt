package net.rsprox.proxy.rs3.window

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * Note: revision 950 ClientPipe / RS2LauncherConnection protocol, verified against the native launcher.
 * This is separate from game traffic and must never be recorded in a session binary or transcript.
 */
internal class Rs3LauncherProtocol(
    private val cacheRoot: String,
    private val userRoot: String,
    private val placement: ByteArray,
    private val send: (ByteArray) -> Unit,
    private val windowChanged: () -> Unit,
    private val openLegalPage: (Boolean) -> Unit,
) {
    var initialized: Boolean = false
        private set
    var ready: Boolean = false
        private set
    var closing: Boolean = false
        private set

    fun accept(payload: ByteArray) {
        val input = DataInputStream(ByteArrayInputStream(payload))
        val id = input.readUnsignedShort()
        val version = input.readUnsignedShort()
        if (!initialized) {
            require(id == 0 && version == 4) { "Unsupported RS3 launcher handshake: $id/$version" }
            require(input.readUnsignedShort() <= 4) { "RS3 requires a newer launcher protocol" }
            require(input.available() == 0)
            reply(1, 4) {
                writeLong(0)
                cstring(cacheRoot)
                cstring(userRoot)
                writeShort(4)
                writeByte(0)
                writeShort(7) // Title, size limits/defaults, and saved placement.
                cstring("RuneScape")
                for (size in intArrayOf(500, 300, 3840, 2160, 1024, 768)) writeShort(size)
                writeShort(placement.size)
                write(placement)
            }
            initialized = true
            return
        }
        require(version == 1) { "Unsupported RS3 launcher message version: $id/$version" }
        when (id) {
            2 -> input.readLong() // Native window-handle notification.
            3 -> {
                check(!ready) { "Duplicate RS3 window-ready notification" }
                // The client has created its hidden window. It applies placement before showing it.
                reply(31) { writeByte(1) }
                ready = true
            }
            4, 5, 12, 19, 27 -> Unit // Focus, startup/cache completion and launcher-UI notifications.
            9, 22, 24 -> input.readUnsignedByte()
            13 -> input.readFloat() // Loading progress belongs to the official launcher's splash screen.
            21 -> {
                input.readLong()
                input.readLong()
            }
            26 -> {
                input.readUnsignedShort() // Window centre, used by the official launcher's UI.
                input.readUnsignedShort()
                require(input.readUnsignedShort() == 34) { "Unexpected RS3 window-placement size" }
                val nativePlacement = ByteArray(34)
                input.readFully(nativePlacement)
                // Query the exact process, using paired Win32 placement coordinates. The native blob
                // mixes screen and workspace coordinates depending on whether the window is maximized.
                if (ready) windowChanged()
            }
            29, 30 -> openLegalPage(id == 30)
            32 -> {
                closing = true
                reply(11) {} // User closed the client; acknowledge the native close request.
            }
            15, 16, 17, 18 -> error("Unexpected RS3 cache relocation request; RSProx does not relocate the cache")
            else -> error("Unsupported RS3 launcher message: $id/$version")
        }
        require(input.available() == 0) { "Unexpected RS3 launcher message length: $id/$version" }
    }

    private fun reply(
        id: Int,
        version: Int = 1,
        body: DataOutputStream.() -> Unit,
    ) {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use {
            it.writeShort(id)
            it.writeShort(version)
            it.body()
        }
        send(bytes.toByteArray())
    }

    private fun DataOutputStream.cstring(value: String) {
        require('\u0000' !in value)
        write(value.toByteArray(Charsets.UTF_8))
        writeByte(0)
    }
}
