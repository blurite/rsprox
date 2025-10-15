package net.rsprox.proxy.target

import java.nio.file.Path

public data class ProxyTargetImportResult(
    val addedCount: Int,
    val replacedCount: Int,
    val skippedTargets: List<String>,
    val destination: Path,
) {
    public val totalImported: Int
        get() = addedCount + replacedCount
}
