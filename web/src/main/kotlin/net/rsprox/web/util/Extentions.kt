package net.rsprox.web.util

import java.security.MessageDigest
import java.util.*

public fun ByteArray.checksum(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val hashBytes = digest.digest(this)
    return hashBytes.toBase64()
}

public fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encodeToString(this)
}
