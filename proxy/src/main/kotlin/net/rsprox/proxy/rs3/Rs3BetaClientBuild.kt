package net.rsprox.proxy.rs3

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

/** A beta flag alone does not identify a packet obfuscation. */
internal object Rs3BetaClientBuild {
    // Note(revision): Add a build only after verifying its packet tables, payloads and patch sites.
    private val windowsOpenGl950Beta1 =
        setOf(
            "e2f471f0dfca8f584f7edd4ea0321bbedb40c95331791312e19c19aa1fb8054f",
            "15e711390ed2ec98efa8d203ede71c58a62e52b0a842f2feef09257ca9828163",
        )
    private val windowsVulkan950Beta1 =
        setOf("81c68f8f1659958916a4661569b7d30a9c7dcbbc39d509fad801a69ffae1c7db")

    fun verify(
        path: Path,
        binaryType: Int,
    ): Rs3ProtocolRevision {
        val supportedBuilds =
            when (binaryType) {
                2 -> windowsOpenGl950Beta1
                10 -> windowsVulkan950Beta1
                else -> error("Unsupported RuneScape 3 beta binary type: $binaryType")
            }
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { input ->
            val bytes = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(bytes)
                if (count == -1) break
                digest.update(bytes, 0, count)
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it.toInt() and 255) }
        require(hash in supportedBuilds) {
            "Unverified RuneScape 3 beta build (SHA-256 $hash). " +
                "Its protocol must be checked before capture; no client was launched."
        }
        return Rs3ProtocolRevision(Rs3ProtocolRevision.BETA_950_1)
    }
}
