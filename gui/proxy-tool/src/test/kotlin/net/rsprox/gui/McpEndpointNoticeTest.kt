package net.rsprox.gui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class McpEndpointNoticeTest {
    @Test
    fun `turning the endpoint on says where it is served after a restart and what it exposes`() {
        assertEquals(
            "RSProx serves the MCP endpoint on http://127.0.0.1:43999/mcp after a restart.\n\n" +
                "The endpoint has no password. Any program on this computer can read the packets of every " +
                "session through it, chat included.",
            mcpRestartNotice(enabledAtStart = false, enabled = true, port = 43999),
        )
    }

    @Test
    fun `turning the endpoint off says that it is served until a restart`() {
        assertEquals(
            "RSProx keeps serving the MCP endpoint until it is restarted.",
            mcpRestartNotice(enabledAtStart = true, enabled = false, port = 43999),
        )
    }

    @Test
    fun `switching back to what this run started with needs no notice`() {
        assertNull(mcpRestartNotice(enabledAtStart = true, enabled = true, port = 43999))
        assertNull(mcpRestartNotice(enabledAtStart = false, enabled = false, port = 43999))
    }
}
