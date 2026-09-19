package net.rsprox.proxy.rs3.window

import com.github.michaelbull.logging.InlineLogger
import com.sun.jna.Native
import com.sun.jna.platform.win32.Kernel32
import com.sun.jna.platform.win32.Shell32Util
import com.sun.jna.platform.win32.ShlObj
import com.sun.jna.platform.win32.WinBase
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.nio.file.Path
import java.security.SecureRandom
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/** One private launcher connection and persistent window slot per client process. */
internal class Rs3LauncherConnection private constructor(
    val id: String,
    private val store: Rs3WindowStateStore,
    private val inbound: HANDLE,
    private val outbound: HANDLE,
    private val config: Map<String, String>,
) : AutoCloseable {
    private val stopped = AtomicBoolean()
    private val finished = CompletableFuture<Unit>()
    private val ready = CompletableFuture<Unit>()
    private var worker: Thread? = null

    @Synchronized
    private fun start(process: ProcessHandle) {
        check(worker == null && !stopped.get()) { "RS3 launcher connection already started/closed" }
        worker = thread(name = "rs3-launcher-${process.pid()}", isDaemon = true) { run(process) }
    }

    fun attach(process: ProcessHandle) {
        start(process)
        process.onExit().thenRun { close() }
        try {
            ready.get(30, TimeUnit.SECONDS)
        } catch (e: Exception) {
            // Do not strand an invisible client if startup IPC fails.
            try {
                close()
            } finally {
                process.destroy()
            }
            throw IllegalStateException("RS3 launcher startup failed", e)
        }
    }

    private fun run(process: ProcessHandle) {
        var closing = false
        try {
            val windows = Rs3WindowApi()
            windows.withDpiAwareness {
                var previous = store.read()
                var reportedSaveFailure = false
                val protocol =
                    Rs3LauncherProtocol(
                        // These are the client's own defaults, not new cache locations.
                        Path.of(Shell32Util.getFolderPath(ShlObj.CSIDL_COMMON_APPDATA), "Jagex").toString(),
                        Path.of(Shell32Util.getFolderPath(ShlObj.CSIDL_LOCAL_APPDATA), "Jagex").toString(),
                        windows.startupPlacement(previous),
                        ::send,
                        {
                            val window = windows.find(process.pid())
                            val placement = window?.let(windows::read)
                            if (placement != null && placement != previous) {
                                try {
                                    store.write(placement)
                                } catch (e: IOException) {
                                    // Saving is optional; an unwritable state file must not break the close handshake.
                                    if (!reportedSaveFailure) {
                                        logger.warn(e) { "Unable to save RS3 window placement" }
                                        reportedSaveFailure = true
                                    }
                                }
                                previous = placement
                            }
                        },
                        ::openLegalPage,
                    )
                val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(25)
                connect(inbound, process, deadline)
                connect(outbound, process, deadline)
                var pending = ByteArray(0)
                val available = IntByReference()
                val received = IntByReference()
                val chunk = ByteArray(4096)
                while (!stopped.get() && process.isAlive) {
                    if (!protocol.ready) check(System.nanoTime() < deadline) { "RS3 launcher startup timed out" }
                    if (!kernel.PeekNamedPipe(inbound, null, 0, null, available, null)) {
                        throw pipeError("read")
                    }
                    if (available.value > 0) {
                        check(kernel.ReadFile(inbound, chunk, minOf(chunk.size, available.value), received, null)) {
                            "Unable to read RS3 launcher pipe: ${kernel.GetLastError()}"
                        }
                        pending += chunk.copyOf(received.value)
                        var offset = 0
                        while (pending.size - offset >= 2) {
                            val length =
                                (pending[offset].toInt() and 255) or
                                    ((pending[offset + 1].toInt() and 255) shl 8)
                            require(length >= 4) { "Invalid RS3 launcher frame length" }
                            if (pending.size - offset < length + 2) break
                            protocol.accept(pending.copyOfRange(offset + 2, offset + 2 + length))
                            closing = protocol.closing
                            if (protocol.ready) ready.complete(Unit)
                            offset += length + 2
                        }
                        pending = pending.copyOfRange(offset, pending.size)
                    } else {
                        Thread.sleep(20)
                    }
                }
            }
        } catch (e: Exception) {
            if (!stopped.get()) {
                ready.completeExceptionally(e)
                if (!closing && process.isAlive) {
                    logger.warn(e) { "RS3 launcher connection ended unexpectedly" }
                }
            }
        } finally {
            ready.completeExceptionally(IllegalStateException("RS3 client exited before its window was ready"))
            release()
        }
    }

    private fun connect(
        pipe: HANDLE,
        process: ProcessHandle,
        deadline: Long,
    ) {
        while (!stopped.get() && process.isAlive) {
            val connected = kernel.ConnectNamedPipe(pipe, null)
            val error = if (connected) 0 else kernel.GetLastError()
            if (connected || error == 535) { // ERROR_PIPE_CONNECTED
                val pid = IntByReference()
                check(kernel.GetNamedPipeClientProcessId(pipe, pid)) { "Cannot identify RS3 launcher pipe peer" }
                check(Integer.toUnsignedLong(pid.value) == process.pid()) { "Unexpected RS3 launcher pipe peer" }
                return
            }
            check(error == 536) { "Cannot connect RS3 launcher pipe: $error" } // ERROR_PIPE_LISTENING
            check(System.nanoTime() < deadline) { "RS3 launcher pipe connection timed out" }
            Thread.sleep(20)
        }
        error("RS3 launcher connection cancelled")
    }

    private fun send(payload: ByteArray) {
        require(payload.size in 4..65535)
        // Only the outer transport length is little-endian. The message itself is big-endian.
        val frame = byteArrayOf(payload.size.toByte(), (payload.size ushr 8).toByte()) + payload
        val written = IntByReference()
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        var offset = 0
        while (offset < frame.size && !stopped.get()) {
            val remaining = frame.copyOfRange(offset, frame.size)
            if (!kernel.WriteFile(outbound, remaining, remaining.size, written, null)) throw pipeError("write")
            offset += written.value
            if (written.value == 0) {
                check(System.nanoTime() < deadline) { "RS3 launcher pipe write timed out" }
                Thread.sleep(20)
            }
        }
        check(offset == frame.size) { "RS3 launcher connection cancelled during write" }
    }

    private fun openLegalPage(privacy: Boolean) {
        val key = if (privacy) "privacyurl" else "termsurl"
        val value = config[key] ?: return
        // Browser launch must not stall pipe processing or prevent the user from closing the client.
        CompletableFuture.runAsync {
            try {
                val uri = URI.create(value)
                require(uri.scheme == "https" || uri.scheme == "http")
                Desktop.getDesktop().browse(uri)
            } catch (_: Exception) {
                // Do not log URLs or exception messages that could contain authentication tokens.
                logger.warn { "Unable to open RS3 legal-information page" }
            }
        }
    }

    override fun close() {
        val running =
            synchronized(this) {
                if (!stopped.compareAndSet(false, true)) return
                worker.also { if (it == null) release() }
            }
        if (running != null && running !== Thread.currentThread()) finished.get(5, TimeUnit.SECONDS)
    }

    private fun release() {
        try {
            kernel.CloseHandle(inbound)
            kernel.CloseHandle(outbound)
            store.close()
        } finally {
            finished.complete(Unit)
        }
    }

    companion object {
        private val logger = InlineLogger()
        private val random = SecureRandom()
        private val kernel = Native.load("kernel32", PipeKernel32::class.java, W32APIOptions.DEFAULT_OPTIONS)

        fun open(
            directory: Path,
            javConfig: String,
        ): Rs3LauncherConnection {
            val config =
                javConfig
                    .lineSequence()
                    .map { it.removePrefix("msg=") }
                    .filter { '=' in it }
                    .associate { it.substringBefore('=') to it.substringAfter('=') }
            val store = Rs3WindowStateStore.acquire(directory)
            try {
                val id =
                    java.lang.Long
                        .toUnsignedString(random.nextLong().or(1), 16)
                        .uppercase()
                val name = "\\\\.\\pipe\\RS2LauncherConnection_$id"
                val inbound = createPipe("${name}_i", 1)
                try {
                    val outbound = createPipe("${name}_o", 2)
                    try {
                        return Rs3LauncherConnection(id, store, inbound, outbound, config)
                    } catch (e: Throwable) {
                        kernel.CloseHandle(outbound)
                        throw e
                    }
                } catch (e: Throwable) {
                    kernel.CloseHandle(inbound)
                    throw e
                }
            } catch (e: Throwable) {
                store.close()
                throw e
            }
        }

        private fun createPipe(
            name: String,
            access: Int,
        ): HANDLE {
            // First-instance-only, nonblocking byte streams, local clients only. Never inherited by children.
            val pipe = kernel.CreateNamedPipe(name, access or 0x80000, 1 or 8, 1, 65536, 65536, 0, null)
            check(pipe != WinBase.INVALID_HANDLE_VALUE) { "Cannot create RS3 launcher pipe: ${kernel.GetLastError()}" }
            return pipe
        }

        private fun pipeError(operation: String): IOException =
            IOException("RS3 launcher pipe $operation failed: ${kernel.GetLastError()}")
    }

    internal interface PipeKernel32 : Kernel32 {
        @Suppress("FunctionName")
        fun GetNamedPipeClientProcessId(
            pipe: HANDLE,
            processId: IntByReference,
        ): Boolean
    }
}
