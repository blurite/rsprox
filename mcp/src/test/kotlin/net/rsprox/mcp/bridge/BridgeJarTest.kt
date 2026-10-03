package net.rsprox.mcp.bridge

import net.rsprox.mcp.session.target
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.util.zip.ZipFile
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BridgeJarTest {
    private val home: Path = Files.createTempDirectory("mcp-jar-test")
    private var embedded = byteArrayOf(1, 2, 3)
    private val custom = target(1, "My Server")
    private val longAgo = FileTime.fromMillis(1_000_000)

    private fun jar(overrideDir: Path? = null) = BridgeJar(overrideDir, home) { embedded }

    @AfterTest
    fun cleanUp() {
        home.toFile().deleteRecursively()
    }

    @Test
    fun `a custom target gets the jar in the custom client's sideload directory`() {
        val installed = jar().installFor(custom)
        assertEquals(home.resolve(".rlcustom/sideloaded-plugins/rsprox-mcp-bridge.jar"), installed)
        assertContentEquals(embedded, Files.readAllBytes(installed))
    }

    @Test
    fun `the official target gets the jar in the official client's sideload directory`() {
        val installed = jar().installFor(target(0, "Old School RuneScape"))
        assertEquals(home.resolve(".runelite/sideloaded-plugins/rsprox-mcp-bridge.jar"), installed)
        assertContentEquals(embedded, Files.readAllBytes(installed))
    }

    @Test
    fun `an override directory is used for every target and leaves the defaults alone`() {
        val dir = home.resolve("elsewhere")
        assertEquals(dir.resolve("rsprox-mcp-bridge.jar"), jar(dir).installFor(custom))
        assertEquals(dir.resolve("rsprox-mcp-bridge.jar"), jar(dir).installFor(target(0, "Old School RuneScape")))
        assertEquals(listOf("elsewhere"), home.toFile().list()?.toList())
    }

    @Test
    fun `an identical installed jar is left untouched`() {
        val installed = jar().installFor(custom)
        Files.setLastModifiedTime(installed, longAgo)

        jar().installFor(custom)

        assertEquals(longAgo, Files.getLastModifiedTime(installed))
    }

    @Test
    fun `a different installed jar is replaced and nothing else is left in the directory`() {
        val installed = jar().installFor(custom)
        Files.setLastModifiedTime(installed, longAgo)
        embedded = byteArrayOf(9, 9, 9, 9)

        jar().installFor(custom)

        assertContentEquals(embedded, Files.readAllBytes(installed))
        assertEquals(1, Files.list(installed.parent).use { it.count() })
    }

    @Test
    fun `the build embeds the plugin jar`() {
        val installed = BridgeJar(home.resolve("embedded")).installFor(custom)
        ZipFile(installed.toFile()).use { zip ->
            assertNotNull(zip.getEntry("net/rsprox/mcpbridge/McpBridgePlugin.class"))
        }
    }
}
