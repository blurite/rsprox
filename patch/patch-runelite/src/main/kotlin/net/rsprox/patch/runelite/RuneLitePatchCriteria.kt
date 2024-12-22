package net.rsprox.patch.runelite

import net.rsprox.patch.PatchCriteria
import net.rsprox.patch.PatchCriteriaBuilder
import java.nio.file.Path

public data class RuneLitePatchCriteria(
    val rsa: String? = null,
    val javConfigUrl: String,
    val port: Int,
    val artifactsDir: Path? = null,
    val bootstrap: Bootstrap? = null
) : PatchCriteria {
    public class Builder : PatchCriteriaBuilder<RuneLitePatchCriteria> {
        private var rsaModulus: String? = null
        private var javConfig: String = DEFAULT_JAVCONFIG_URL
        private var port: Int = DEFAULT_PORT
        private var artifactsDir: Path? = null
        private var bootstrap: Bootstrap? = null

        override fun port(port: Int): PatchCriteriaBuilder<RuneLitePatchCriteria> {
            this.port = port
            return this
        }

        override fun rsaModulus(hexString: String): Builder {
            rsaModulus = hexString
            return this
        }

        override fun acceptAllHosts(): Builder {
            return this
        }

        override fun javConfig(url: String): Builder {
            this.javConfig = url
            return this
        }

        override fun worldList(url: String): Builder {
            return this
        }

        override fun varpCount(expectedVarpCount: Int, replacementVarpCount: Int): Builder {
            return this
        }

        override fun siteUrl(replacement: String): Builder {
            return this
        }

        override fun name(replacement: String): Builder {
            return this
        }

        public fun setArtifactsDir(path: Path): Builder {
            this.artifactsDir = path
            return this
        }

        public fun setBootstrap(bootstrap: Bootstrap): Builder {
            this.bootstrap = bootstrap
            return this
        }

        override fun build(): RuneLitePatchCriteria {
            return RuneLitePatchCriteria(
                rsa = rsaModulus,
                javConfigUrl = this.javConfig,
                port = port,
                artifactsDir = artifactsDir,
                bootstrap = bootstrap
            )
        }

    }

    public companion object {
        private const val DEFAULT_PORT: Int = 43594
        private const val DEFAULT_JAVCONFIG_URL: String = "http://oldschool.runescape.com/jav_config.ws?m=0"
    }
}
