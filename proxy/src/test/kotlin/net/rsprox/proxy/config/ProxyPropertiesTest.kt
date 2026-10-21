package net.rsprox.proxy.config

import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ProxyPropertiesTest {
    @Test
    fun `a properties file from before the MCP endpoint reads it as off, at the default port`() {
        val file = Files.createTempFile("proxy", ".properties")
        Files.writeString(file, "app.theme=RuneLite\n")

        val properties = ProxyProperties(file)

        assertEquals(false, properties.getProperty(ProxyProperty.MCP_ENABLED))
        assertEquals(43580, properties.getProperty(ProxyProperty.MCP_PORT))
    }

    @Test
    fun `a new properties file is written with the MCP endpoint off`() {
        val file = Files.createTempDirectory("proxy").resolve("proxy.properties")

        assertEquals(false, ProxyProperties(file).getProperty(ProxyProperty.MCP_ENABLED))
        assertContains(Files.readAllLines(file), "mcp.enabled=false")
    }

    @Test
    fun `a properties file that turns the MCP endpoint on and moves it to another port is kept`() {
        val file = Files.createTempFile("proxy", ".properties")
        Files.writeString(file, "mcp.enabled=true\nmcp.port=43999\n")

        val properties = ProxyProperties(file)

        assertEquals(true, properties.getProperty(ProxyProperty.MCP_ENABLED))
        assertEquals(43999, properties.getProperty(ProxyProperty.MCP_PORT))
    }

    @Test
    fun `an MCP endpoint that was turned on and saved is on when the file is read again`() {
        val file = Files.createTempFile("proxy", ".properties")
        Files.writeString(file, "app.theme=RuneLite\n")
        val properties = ProxyProperties(file)

        properties.setProperty(ProxyProperty.MCP_ENABLED, true)
        properties.saveProperties(file)

        assertEquals(true, ProxyProperties(file).getProperty(ProxyProperty.MCP_ENABLED))
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
