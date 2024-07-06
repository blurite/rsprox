package net.rsprox.proxy.rsa

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.config.CONFIGURATION_PATH
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.nio.file.Files

private val RSA_FILE = CONFIGURATION_PATH.resolve("key.rsa")
private val logger = InlineLogger()

internal fun readOrGenerateRsaKey(): RSAPrivateCrtKeyParameters {
    return if (Files.exists(RSA_FILE)) {
        val key = Rsa.readPrivateKey(RSA_FILE)
        logger.debug { "RSA key loaded from $RSA_FILE" }
        key
    } else {
        val (_, private) = Rsa.generateKeyPair(Rsa.CLIENT_KEY_LENGTH)
        Rsa.writePrivateKey(RSA_FILE, private)
        logger.debug { "RSA key generated to $RSA_FILE" }
        private
    }
}
