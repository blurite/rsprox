package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonProperty

public data class ProxyTargetConfigList(
    @JsonProperty("config")
    public val entries: List<ProxyTargetConfig>,
)
