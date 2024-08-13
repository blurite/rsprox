package net.rsprox.web.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

public fun ByteArray.checksum(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val hashBytes = digest.digest(this)
    return hashBytes.toBase64()
}

public fun ByteArray.toBase64(): String {
    return Base64.getUrlEncoder().encodeToString(this)
}

public fun ByteArray.zip(fileName: String): ByteArray {
    val bos = ByteArrayOutputStream()
    ZipOutputStream(bos).use { zip ->
        val entry = ZipEntry(fileName)
        zip.putNextEntry(entry)
        zip.write(this)
        zip.closeEntry()
    }
    return bos.toByteArray()
}

public fun ByteArray.unzip(): ByteArray {
    val bis = ByteArrayInputStream(this)
    ZipInputStream(bis).use { zip ->
        zip.nextEntry
        return zip.readBytes()
    }
}
