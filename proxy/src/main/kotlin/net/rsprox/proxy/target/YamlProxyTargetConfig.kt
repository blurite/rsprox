package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists

@JsonIgnoreProperties(ignoreUnknown = true)
public data class YamlProxyTargetConfig(
    @field:JsonProperty("name")
    public val name: String,
    @field:JsonProperty("jav_config_url")
    public val javConfigUrl: String,
    @field:JsonProperty("modulus")
    public val modulus: String? = null,
    @field:JsonProperty("varp_count")
    public val varpCount: Int = DEFAULT_VARP_COUNT,
    @field:JsonProperty("revision")
    public val revision: String? = null,
    @field:JsonProperty("runelite_bootstrap_commithash")
    public val runeliteBootstrapCommitHash: String? = null,
    @field:JsonProperty("runelite_gamepack_url")
    public val runeliteGamepackUrl: String? = null,
    @field:JsonProperty("binary_folder")
    public val binaryFolder: String? = null,
    @field:JsonProperty("export_binaries")
    public val exportBinaries: Boolean = true,
    @field:JsonProperty("game_server_port")
    public val gameServerPort: Int = ProxyTargetConfig.DEFAULT_GAME_SERVER_PORT,
) {
    public companion object {
        public const val DEFAULT_NAME: String = "Old School RuneScape"
        public const val DEFAULT_VARP_COUNT: Int = 5000

        private val objectMapper =
            ObjectMapper(YAMLFactory())
                .registerKotlinModule()
                .findAndRegisterModules()

        public fun load(path: Path): YamlProxyTargetConfigList {
            if (!path.exists()) return YamlProxyTargetConfigList(emptyList())
            val root = objectMapper.readTree(path.toFile())
            return YamlProxyTargetConfigList.fromNode(root)
        }

        public fun parse(inputStream: InputStream): YamlProxyTargetConfigList {
            inputStream.use { stream ->
                val root = objectMapper.readTree(stream)
                return YamlProxyTargetConfigList.fromNode(root)
            }
        }
    }
}
