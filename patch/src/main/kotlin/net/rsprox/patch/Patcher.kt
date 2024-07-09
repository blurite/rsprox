package net.rsprox.patch

import java.nio.file.Path

public fun interface Patcher {
    public fun patch(
        path: Path,
        rsa: String,
        javConfigUrl: String,
        worldListUrl: String,
        port: Int,
    ): PatchResult
}
