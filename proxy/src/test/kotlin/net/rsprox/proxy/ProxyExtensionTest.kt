package net.rsprox.proxy

import io.netty.buffer.ByteBufAllocator
import kotlin.test.Test
import kotlin.test.assertEquals

/** Extensions that the test registers through `META-INF/services`. */
class FailingExtension : ProxyExtension {
    override fun start(service: ProxyService): Unit = throw IllegalStateException("cannot start")
}

class RecordingExtension : ProxyExtension {
    override fun start(service: ProxyService) {
        started += service
    }

    companion object {
        val started = ArrayList<ProxyService>()
    }
}

class ProxyExtensionTest {
    @Test
    fun `an extension that fails to start does not keep the next one from starting`() {
        val service = ProxyService(ByteBufAllocator.DEFAULT)

        ProxyExtension.startAll(service)

        assertEquals(listOf(service), RecordingExtension.started)
    }
}
