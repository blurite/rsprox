package net.rsprox.proxy.rsa

import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path

private val RSA_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
private val RSA_FILE = RSA_PATH.resolve("key.rsa")

internal fun readOrGenerateRsaKey(): RSAPrivateCrtKeyParameters {
    return if (Files.exists(RSA_FILE)) {
        Rsa.readPrivateKey(RSA_FILE)
    } else {
        Files.createDirectories(RSA_PATH)
        val (_, private) = Rsa.generateKeyPair(Rsa.CLIENT_KEY_LENGTH)
        Rsa.writePrivateKey(RSA_FILE, private)
        private
    }
}
