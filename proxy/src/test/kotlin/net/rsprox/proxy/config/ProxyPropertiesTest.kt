package net.rsprox.proxy.config

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class ProxyPropertiesTest {
    @Test
    fun `a properties file from before the MCP endpoint reads it as on, at the default port`() {
        val file = Files.createTempFile("proxy", ".properties")
        Files.writeString(file, "app.theme=RuneLite\n")

        val properties = ProxyProperties(file)

        assertEquals(true, properties.getProperty(ProxyProperty.MCP_ENABLED))
        assertEquals(43580, properties.getProperty(ProxyProperty.MCP_PORT))
    }

    @Test
    fun `the MCP endpoint can be turned off and moved to another port`() {
        val file = Files.createTempFile("proxy", ".properties")
        Files.writeString(file, "mcp.enabled=false\nmcp.port=43999\n")

        val properties = ProxyProperties(file)

        assertEquals(false, properties.getProperty(ProxyProperty.MCP_ENABLED))
        assertEquals(43999, properties.getProperty(ProxyProperty.MCP_PORT))
    }

    @Test
    fun `the bridge plugin is installed unless the file turns it off`() {
        val file = Files.createTempFile("proxy", ".properties")
        Files.writeString(file, "app.theme=RuneLite\n")

        assertEquals(true, ProxyProperties(file).getProperty(ProxyProperty.MCP_PLUGIN))

        Files.writeString(file, "mcp.plugin=false\n")

        assertEquals(false, ProxyProperties(file).getProperty(ProxyProperty.MCP_PLUGIN))
    }
}
