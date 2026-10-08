package net.rsprox.proxy

import io.netty.buffer.ByteBufAllocator
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** Extensions that the test registers through `META-INF/services`. */
class FailingExtension : ProxyExtension {
    override fun start(service: ProxyService): Unit = throw IllegalStateException("cannot start")

    override fun serve(): String = error("an extension that failed to start is not asked to serve")

    override fun stopServing(): Unit = error("an extension that failed to start is not asked to stop serving")
}

class RecordingExtension : ProxyExtension {
    override fun start(service: ProxyService) {
        started += service
    }

    override fun serve(): String? {
        asked += "serve"

        return refusal
    }

    override fun stopServing() {
        asked += "stop serving"
    }

    companion object {
        val started = ArrayList<ProxyService>()
        val asked = ArrayList<String>()
        var refusal: String? = null
    }
}

class ProxyExtensionTest {
    @BeforeTest
    fun reset() {
        RecordingExtension.started.clear()
        RecordingExtension.asked.clear()
        RecordingExtension.refusal = null
    }

    @Test
    fun `an extension that fails to start does not keep the next one from starting`() {
        val service = ProxyService(ByteBufAllocator.DEFAULT)

        ProxyExtension.startAll(service)

        assertEquals(listOf(service), RecordingExtension.started)
    }

    @Test
    fun `the started extensions are asked to serve and to stop serving, and one that failed to start is not`() {
        val extensions = ProxyExtension.startAll(ProxyService(ByteBufAllocator.DEFAULT))

        assertNull(extensions.serve())
        extensions.stopServing()

        assertEquals(listOf("serve", "stop serving"), RecordingExtension.asked)
    }

    @Test
    fun `serving reports why an extension cannot serve`() {
        val extensions = ProxyExtension.startAll(ProxyService(ByteBufAllocator.DEFAULT))
        RecordingExtension.refusal = "127.0.0.1:43580 cannot be bound"

        assertEquals("127.0.0.1:43580 cannot be bound", extensions.serve())
    }

    @Test
    fun `serving reports an extension that throws instead of throwing`() {
        val extensions = StartedExtensions(listOf(FailingExtension()))

        assertEquals(
            "net.rsprox.proxy.FailingExtension failed: an extension that failed to start is not asked to serve",
            extensions.serve(),
        )
    }

    @Test
    fun `serving with no extension started reports that nothing is served`() {
        assertEquals(
            "Nothing is served, because no extension of RSProx is running. The log names one that failed to start.",
            StartedExtensions(emptyList()).serve(),
        )
    }
}
