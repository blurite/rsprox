package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import net.rsprox.proxy.config.HTTP_SERVER_PORT
import java.nio.file.Path
import kotlin.io.path.exists

@JsonIgnoreProperties(ignoreUnknown = true)
public data class ProxyTargetConfig(
    public val id: Int,
    public val name: String,
    @JsonProperty("jav_config_url")
    public val javConfigUrl: String,
    public val modulus: String? = null,
    @JsonProperty("varp_count")
    public val varpCount: Int = DEFAULT_VARP_COUNT,
    public val revision: String? = null,
) {
    public val httpPort: Int
        get() = HTTP_SERVER_PORT + id

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
