package net.rsprox.gui

import java.nio.file.Files
import java.nio.file.Path
import java.util.prefs.Preferences

public object RecentReplayDumps {
    private val preferences: Preferences =
        Preferences.userNodeForPackage(App::class.java).node("recentReplayDumps")

    public fun record(path: Path) {
        val normalized =
            runCatching { path.toRealPath() }
                .getOrElse { path.toAbsolutePath().normalize() }
        val paths = buildList {
            add(normalized)
            addAll(loadPaths().filterNot { it == normalized })
        }
        savePaths(paths.take(MAX_RECENTS).toList())
    }

    public fun list(limit: Int): List<Path> {
        return loadPaths()
            .filter { path ->
                Files.isRegularFile(path) &&
                    path.fileName?.toString()?.endsWith(".bin", ignoreCase = true) == true
            }
            .take(limit)
    }

    private fun loadPaths(): List<Path> {
        return preferences
            .get(RECENTS_KEY, "")
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { Path.of(it) }
            .distinct()
            .toList()
    }

    private fun savePaths(paths: List<Path>) {
        preferences.put(
            RECENTS_KEY,
            paths.joinToString(separator = "\n") { it.toString() },
        )
        runCatching { preferences.flush() }
    }

    private const val RECENTS_KEY = "paths"
    private const val MAX_RECENTS = 12
}
