package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonProperty

public data class YamlProxyTargetConfigList(
    @field:JsonProperty("config")
    public val entries: List<YamlProxyTargetConfig>,
)
