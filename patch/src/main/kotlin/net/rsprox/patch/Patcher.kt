package net.rsprox.patch

import java.nio.file.Path

public fun interface Patcher<T> {
    public fun patch(
        path: Path,
        rsa: String,
        javConfigUrl: String,
        worldListUrl: String,
        port: Int,
        metadata: T,
    ): PatchResult
}
