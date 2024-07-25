package net.rsprox.cache.live

import net.rsprot.crypto.xtea.XteaKey
import net.rsprox.cache.Js5MasterIndex

public data class LiveConnectionInfo(
    public val host: String,
    public val port: Int,
    public val revision: Int,
    public val key: XteaKey,
    public val masterIndex: Js5MasterIndex,
)
