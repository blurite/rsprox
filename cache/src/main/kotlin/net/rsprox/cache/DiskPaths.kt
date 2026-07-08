package net.rsprox.cache

import java.nio.file.Path
import kotlin.io.path.Path

internal val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
internal val CACHES_DIRECTORY: Path = CONFIGURATION_PATH.resolve("caches")
internal val OPENRS2_DISK_CACHES_DIRECTORY: Path = CACHES_DIRECTORY.resolve("openrs2-disk")
internal val DISK_CACHE_DICTIONARY_PATH: Path = CACHES_DIRECTORY.resolve("dictionary.json")
