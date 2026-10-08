package net.rsprox.proxy.util

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.property.ChildProperty
import net.rsprox.shared.property.RootProperty
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class FanOutSessionMonitorTest {
    private class Recorder(
        private val label: String = "",
        private val failing: Boolean = false,
    ) : SessionMonitor<String> {
        val calls = ArrayList<String>()
        val logins = ArrayList<Recorder>()

        private fun record(call: String) {
            calls += label + call
            if (failing) throw IllegalStateException("$label$call failed")
        }

        override fun forSession(header: String): SessionMonitor<String> {
            record("forSession $header")

            return Recorder("$header:").also { logins += it }
        }

        override fun onPacketDirection(direction: StreamDirection) = record("direction $direction")

        override fun onLogin(header: String) = record("login $header")

        override fun onLogout(header: String) = record("logout $header")

        override fun onCacheUpdate(cacheProvider: CacheProvider) = record("cache")

        override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) = record("in $bytesPerLastSecond")

        override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) = record("out $bytesPerLastSecond")

        override fun onNameUpdate(name: String) = record("name $name")

        override fun onUserInformationUpdate(
            userId: Long,
            userHash: Long,
        ) = record("user $userId $userHash")

        override fun onTranscribe(
            cycle: Int,
            property: RootProperty,
        ) = record("transcribe $cycle ${property.prot}")
    }

    private val packet =
        object : RootProperty {
            override val prot = "IF_SETTEXT"
            override val children = mutableListOf<ChildProperty<*>>()
        }

    private fun everyCallback(monitor: SessionMonitor<String>) {
        monitor.onLogin("a")
        monitor.onUserInformationUpdate(1, 2)
        monitor.onCacheUpdate { error("not read") }
        monitor.onPacketDirection(StreamDirection.CLIENT_TO_SERVER)
        monitor.onTranscribe(7, packet)
        monitor.onNameUpdate("Alice")
        monitor.onIncomingBytesPerSecondUpdate(3)
        monitor.onOutgoingBytesPerSecondUpdate(4)
        monitor.onLogout("a")
    }

    private val everyCall =
        listOf(
            "login a",
            "user 1 2",
            "cache",
            "direction CLIENT_TO_SERVER",
            "transcribe 7 IF_SETTEXT",
            "name Alice",
            "in 3",
            "out 4",
            "logout a",
        )

    @Test
    fun `every callback reaches the primary monitor and each observer, past an observer that throws`() {
        val primary = Recorder()
        val failing = Recorder(failing = true)
        val later = Recorder()

        everyCallback(FanOutSessionMonitor(primary, listOf(failing, later)))

        assertEquals(everyCall, primary.calls)
        assertEquals(everyCall, failing.calls)
        assertEquals(everyCall, later.calls)
    }

    @Test
    fun `the callbacks of a login reach the login monitors, less that of an observer that fails to make one`() {
        val primary = Recorder()
        val failing = Recorder(failing = true)
        val later = Recorder()

        everyCallback(FanOutSessionMonitor(primary, listOf(failing, later)).forSession("a"))

        assertEquals(listOf("forSession a"), primary.calls)
        assertEquals(listOf("forSession a"), failing.calls)
        assertEquals(listOf("forSession a"), later.calls)
        assertEquals(everyCall.map { "a:$it" }, primary.logins.single().calls)
        assertEquals(everyCall.map { "a:$it" }, later.logins.single().calls)
    }

    @Test
    fun `a login without a working observer is monitored by the login monitor of the primary alone`() {
        val primary = Recorder()

        val login = FanOutSessionMonitor(primary, listOf(Recorder(failing = true))).forSession("a")

        assertSame(primary.logins.single(), login)
    }

    @Test
    fun `what the primary monitor throws is passed on after the observers heard the callback`() {
        val observer = Recorder()
        val monitor = FanOutSessionMonitor(Recorder(failing = true), listOf(observer))

        val thrown = assertFailsWith<IllegalStateException> { monitor.onNameUpdate("Alice") }

        assertEquals("name Alice failed", thrown.message)
        assertEquals(listOf("name Alice"), observer.calls)
    }
}
