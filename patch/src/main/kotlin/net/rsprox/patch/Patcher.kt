package net.rsprox.patch

import java.nio.file.Path

public fun interface Patcher<T : PatchCriteria> {
    public fun patch(
        path: Path,
        criteria: T
    ): PatchResult
}
