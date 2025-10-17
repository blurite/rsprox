package net.rsprox.proxy.target

import net.rsprox.proxy.config.CONFIGURATION_PATH
import java.nio.file.Path

internal val PROXY_TARGETS_FILE: Path = CONFIGURATION_PATH.resolve("proxy-targets.yaml")
internal val ALT_PROXY_TARGETS_FILE: Path = CONFIGURATION_PATH.resolve("proxy-targets.yml")
