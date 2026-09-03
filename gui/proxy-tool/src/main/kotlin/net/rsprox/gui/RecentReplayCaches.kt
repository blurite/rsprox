package net.rsprox.gui

import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.prefs.Preferences

internal class RecentReplayCaches(
    private val preferences: Preferences =
        Preferences.userNodeForPackage(App::class.java).node("recentReplayCaches"),
) {
    fun record(path: Path) {
        val normalized = normalize(path)
        save(
            buildList {
                add(normalized)
                addAll(load().filterNot { it == normalized })
            }.take(MAX_RECENTS),
        )
    }

    fun remove(path: Path) {
        val normalized = normalize(path)
        save(load().filterNot { it == normalized })
    }

    fun list(): List<Path> {
        val valid = load().filter(::isCacheDirectory).take(MAX_RECENTS)
        save(valid)
        return valid
    }

    private fun load(): List<Path> {
        return preferences
            .get(RECENTS_KEY, "")
            .lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .map(Path::of)
            .map(::normalize)
            .distinct()
            .toList()
    }

    private fun save(paths: List<Path>) {
        preferences.put(RECENTS_KEY, paths.joinToString("\n"))
        runCatching { preferences.flush() }
    }

    private fun normalize(path: Path): Path =
        runCatching { path.toRealPath() }
            .getOrElse { path.toAbsolutePath().normalize() }

    private fun isCacheDirectory(path: Path): Boolean =
        Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS) &&
            Files.isRegularFile(path.resolve(DATA_FILE), LinkOption.NOFOLLOW_LINKS)

    private companion object {
        private const val RECENTS_KEY: String = "paths"
        private const val MAX_RECENTS: Int = 5
        private const val DATA_FILE: String = "main_file_cache.dat2"
    }
}
