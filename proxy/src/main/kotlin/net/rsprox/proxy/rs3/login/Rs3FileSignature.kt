package net.rsprox.proxy.rs3.login

import org.bouncycastle.crypto.digests.WhirlpoolDigest
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters
import java.math.BigInteger
import java.util.Base64
import java.util.zip.CRC32

public object Rs3FileSignature {
    public fun crc32(data: ByteArray): Long {
        return CRC32().apply { update(data, 0, data.size) }.value
    }

    public fun generateFileHash(
        data: ByteArray,
        launcherPrivateKey: RSAPrivateCrtKeyParameters,
    ): String {
        val digest = WhirlpoolDigest()
        digest.update(data, 0, data.size)
        val whirlpoolHash = ByteArray(digest.digestSize)
        digest.doFinal(whirlpoolHash, 0)

        val hash = ByteArray(1 + whirlpoolHash.size)
        hash[0] = 10
        whirlpoolHash.copyInto(hash, 1)

        val signed =
            BigInteger(hash)
                .modPow(launcherPrivateKey.exponent, launcherPrivateKey.modulus)
                .toByteArray()

        return Base64.getEncoder().encodeToString(signed)
            .replace("+", "*")
            .replace("/", "-")
            .replace("=", "")
    }
}
