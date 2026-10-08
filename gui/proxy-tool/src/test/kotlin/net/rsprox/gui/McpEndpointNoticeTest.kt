package net.rsprox.gui

import kotlin.test.Test
import kotlin.test.assertEquals

class McpEndpointNoticeTest {
    private val calls = ArrayList<String>()

    private fun switch(
        enabled: Boolean,
        refusal: String? = null,
    ): McpEndpointSwitch =
        switchMcpEndpoint(
            enabled,
            serve = {
                calls += "serve"
                refusal
            },
            stopServing = { calls += "stop serving" },
            save = { calls += "save $it" },
        )

    @Test
    fun `turning the endpoint on serves it at once and then saves the choice`() {
        assertEquals(McpEndpointSwitch(served = true, failure = null), switch(enabled = true))
        assertEquals(listOf("serve", "save true"), calls)
    }

    @Test
    fun `turning the endpoint off stops serving it at once and then saves the choice`() {
        assertEquals(McpEndpointSwitch(served = false, failure = null), switch(enabled = false))
        assertEquals(listOf("stop serving", "save false"), calls)
    }

    @Test
    fun `an endpoint that cannot be served stays off, saves nothing and says why with a sentence to a line`() {
        val refusal =
            "The MCP endpoint is not served: 127.0.0.1:43999 cannot be bound (Address already in use). " +
                "Another rsprox may hold it. Close that one."

        assertEquals(
            McpEndpointSwitch(
                served = false,
                failure =
                    "The MCP endpoint is not served: 127.0.0.1:43999 cannot be bound (Address already in use).\n" +
                        "Another rsprox may hold it.\n" +
                        "Close that one.",
            ),
            switch(enabled = true, refusal),
        )
        assertEquals(listOf("serve"), calls)
    }

    @Test
    fun `two clicks in a row leave the endpoint as the second click says`() {
        switch(enabled = true)

        assertEquals(McpEndpointSwitch(served = false, failure = null), switch(enabled = false))
        assertEquals(listOf("serve", "save true", "stop serving", "save false"), calls)
    }
}
