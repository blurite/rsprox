package net.rsprox.proxy.rs3.launcher

import net.rsprox.proxy.rs3.config.Rs3JavConfig
import org.bouncycastle.crypto.digests.WhirlpoolDigest
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import java.util.zip.CRC32

/** Keep the native CRC/signature checks; trust only artifacts signed by this RSProx process. */
internal class Rs3LauncherPatch(
    private val key: KeyPair = signingKey,
) {
    fun patch(
        bootstrap: ByteArray,
        directoryName: String,
    ): ByteArray {
        require(directoryName.matches(Regex("[a-z0-9]{8}")))
        check(Rs3LauncherDistribution.sha256(bootstrap) == Rs3LauncherDistribution.LAUNCHER_SHA256) {
            "Unsupported RuneScape launcher executable"
        }
        var text = bootstrap.toString(Charsets.ISO_8859_1)
        val modulus = Regex("[0-9a-f]{1024}").findAll(text).single().value
        val replacement = (key.public as RSAPublicKey).modulus.toString(16)
        check(replacement.length == modulus.length)
        val directory = "\u0000launcher\u0000"
        check(text.indexOf(directory) >= 0 && text.indexOf(directory) == text.lastIndexOf(directory))
        text = text.replace(modulus, replacement).replace("\u0000launcher\u0000", "\u0000$directoryName\u0000")
        return text.toByteArray(Charsets.ISO_8859_1)
    }

    fun configuration(
        upstream: Rs3JavConfig,
        client: ByteArray,
        codebase: String,
    ): ByteArray {
        val digest = WhirlpoolDigest()
        digest.update(client, 0, client.size)
        val hash = ByteArray(digest.digestSize)
        digest.doFinal(hash, 0)
        val privateKey = key.private as RSAPrivateKey
        val signature = BigInteger(1, byteArrayOf(1) + hash).modPow(privateKey.privateExponent, privateKey.modulus)
        val encoded =
            Base64
                .getEncoder()
                .encodeToString(signature.toByteArray())
                .replace('+', '*')
                .replace('/', '-')
        val fields =
            linkedMapOf(
                "binary_name" to "rs2client.exe",
                "binary_count" to "1",
                "download_name_0" to "rs2client.exe",
                "download_crc_0" to CRC32().apply { update(client) }.value.toString(),
                "download_hash_0" to encoded,
                "launcher_version" to Rs3LauncherDistribution.VERSION.toString(),
                "launcher_sub_version" to Rs3LauncherDistribution.SUB_VERSION.toString(),
                "codebase" to codebase,
            )
        // No self-update, remote executable download, or renderer switch. The proxy already selected
        // and patched the executable. A missing/corrupted local file must fail, not fetch a live client.
        val lines =
            upstream.text
                .lineSequence()
                .filter {
                    val name = it.substringBefore('=')
                    name !in fields && name != "launcher_download_url" && !name.startsWith("download_")
                }.toList() + fields.map { (name, value) -> "$name=$value" }
        return (lines.joinToString("\n") + "\n").toByteArray(Charsets.ISO_8859_1)
    }

    companion object {
        // Ephemeral and never written to disk. Separate from the game's login RSA key.
        private val signingKey: KeyPair by lazy {
            KeyPairGenerator.getInstance("RSA").apply { initialize(4096) }.generateKeyPair()
        }
    }
}
