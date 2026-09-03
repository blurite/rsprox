package net.rsprox.gui

import java.nio.file.Files
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecentReplayCachesTest {
    @Test
    fun `keeps five unique cache directories in most-recent order`() {
        val preferences = Preferences.userRoot().node("rsprox-test/${UUID.randomUUID()}")
        try {
            val store = RecentReplayCaches(preferences)
            val paths =
                List(6) {
                    Files.createTempDirectory("rsprox-recent-cache-$it").also { path ->
                        Files.createFile(path.resolve("main_file_cache.dat2"))
                    }
                }

            paths.forEach(store::record)
            store.record(paths[2])

            val expected = listOf(paths[2], paths[5], paths[4], paths[3], paths[1])
            val actual = store.list()
            assertEquals(5, actual.size)
            assertTrue(expected.zip(actual).all { (left, right) -> Files.isSameFile(left, right) })
        } finally {
            preferences.removeNode()
        }
    }
}
