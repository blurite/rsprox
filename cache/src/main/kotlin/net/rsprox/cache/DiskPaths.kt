package net.rsprox.cache

import java.nio.file.Path
import kotlin.io.path.Path

internal val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
internal val CACHES_DIRECTORY: Path = CONFIGURATION_PATH.resolve("caches")
internal val DISK_CACHE_DICTIONARY_PATH: Path = CACHES_DIRECTORY.resolve("dictionary.json")
