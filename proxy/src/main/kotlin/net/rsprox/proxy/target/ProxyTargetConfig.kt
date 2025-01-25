package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import java.nio.file.Path
import kotlin.io.path.exists

public data class ProxyTargetConfig(
    public val id: Int,
    public val name: String,
    @JsonProperty("jav_config_url")
    public val javConfigUrl: String,
    @JsonProperty("http_port")
    public val httpPort: Int,
    public val modulus: String? = null,
    @JsonProperty("varp_count")
    public val varpCount: Int = DEFAULT_VARP_COUNT,
) {
    public companion object {
        public const val DEFAULT_NAME: String = "Old School RuneScape"
        public const val DEFAULT_VARP_COUNT: Int = 5000

        public fun load(path: Path): ProxyTargetConfigList {
            if (!path.exists()) return ProxyTargetConfigList(emptyList())
            return ObjectMapper(YAMLFactory())
                .findAndRegisterModules()
                .readValue(path.toFile())
        }
    }
}
