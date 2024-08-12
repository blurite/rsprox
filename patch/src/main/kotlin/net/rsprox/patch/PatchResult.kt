package net.rsprox.patch

import java.nio.file.Path

public sealed interface PatchResult {
    public data object Failure : PatchResult

    public data class Success(
        public val oldModulus: String?,
        public val outputPath: Path,
    ) : PatchResult
}
