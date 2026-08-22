package net.rsprox.proxy.rsa

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.proxy.config.CONFIGURATION_PATH
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.nio.file.Files
import java.nio.file.Path

private val RSA_FILE = CONFIGURATION_PATH.resolve("key.rsa")
private val logger = InlineLogger()

internal fun readOrGenerateRsaKey(
    file: Path = RSA_FILE,
    keyLengthBits: Int = Rsa.CLIENT_KEY_LENGTH,
): RSAPrivateCrtKeyParameters {
    return if (Files.exists(file)) {
        val key = Rsa.readPrivateKey(file)
        logger.debug { "RSA key loaded from $file" }
        key
    } else {
        Files.createDirectories(file.parent)
        val (_, private) = Rsa.generateKeyPair(keyLengthBits)
        Rsa.writePrivateKey(file, private)
        logger.debug { "RSA key generated to $file" }
        private
    }
}

public class Rs3PersistedRsaKeyProvider(
    keyFileName: String,
    private val keyLengthBits: Int,
) {
    private val keyFile: Path =
        Path.of(System.getProperty("user.home"), ".rsprox", keyFileName)

    public fun readOrGenerate(): RSAPrivateCrtKeyParameters {
        return readOrGenerateRsaKey(keyFile, keyLengthBits)
    }

    public fun publicModulusHex(key: RSAPrivateCrtKeyParameters): String {
        return key.modulus.toString(16)
    }
}

public val Rs3ProxyRsaKeyProvider: Rs3PersistedRsaKeyProvider =
    Rs3PersistedRsaKeyProvider("rs3-login-key.rsa", Rsa.RS3_LOGIN_KEY_LENGTH)

public val Rs3ProxyLauncherRsaKeyProvider: Rs3PersistedRsaKeyProvider =
    Rs3PersistedRsaKeyProvider("rs3-launcher-key.rsa", Rsa.RS3_LAUNCHER_KEY_LENGTH)
