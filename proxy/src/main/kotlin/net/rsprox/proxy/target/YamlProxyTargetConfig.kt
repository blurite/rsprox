package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Path
import kotlin.io.path.exists

@JsonIgnoreProperties(ignoreUnknown = true)
public data class YamlProxyTargetConfig(
    @JsonProperty("name")
    public val name: String,
    @JsonProperty("jav_config_url")
    public val javConfigUrl: String,
    @JsonProperty("modulus")
    public val modulus: String? = null,
    @JsonProperty("varp_count")
    public val varpCount: Int = DEFAULT_VARP_COUNT,
    @JsonProperty("revision")
    public val revision: String? = null,
    @JsonProperty("runelite_bootstrap_commithash")
    public val runeliteBootstrapCommitHash: String? = null,
    @JsonProperty("runelite_gamepack_url")
    public val runeliteGamepackUrl: String? = null,
) {
    public companion object {
        public const val DEFAULT_NAME: String = "Old School RuneScape"
        public const val DEFAULT_VARP_COUNT: Int = 5000

        public fun load(path: Path): YamlProxyTargetConfigList {
            if (!path.exists()) return YamlProxyTargetConfigList(emptyList())
            return ObjectMapper(YAMLFactory())
                .findAndRegisterModules()
                .readValue(path.toFile())
        }
    }
}
