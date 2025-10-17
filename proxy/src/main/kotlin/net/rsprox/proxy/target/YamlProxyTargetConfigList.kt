package net.rsprox.proxy.target

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

public data class YamlProxyTargetConfigList(
    @field:JsonProperty("config")
    public val entries: List<YamlProxyTargetConfig>,
) {
    public companion object {
        private val objectMapper =
            ObjectMapper(YAMLFactory())
                .registerKotlinModule()
                .findAndRegisterModules()

        public fun fromNode(node: JsonNode?): YamlProxyTargetConfigList {
            if (node == null || node.isNull) {
                return YamlProxyTargetConfigList(emptyList())
            }

            if (node.isArray) {
                val entries = node.mapNotNull { child -> child.toConfigOrNull() }
                return YamlProxyTargetConfigList(entries)
            }

            if (node.isObject) {
                val configNode = node.get("config")
                if (configNode == null || configNode.isNull) {
                    // Support a single target object at the root as well.
                    return node.toConfigOrList()
                }
                return fromNode(configNode)
            }

            return YamlProxyTargetConfigList(emptyList())
        }

        private fun JsonNode?.toConfigOrList(): YamlProxyTargetConfigList {
            if (this == null || this.isNull) {
                return YamlProxyTargetConfigList(emptyList())
            }
            return when {
                this.isArray -> {
                    val entries = this.mapNotNull { child -> child.toConfigOrNull() }
                    YamlProxyTargetConfigList(entries)
                }
                this.isObject -> {
                    val entry = this.toConfigOrNull()
                    if (entry != null) YamlProxyTargetConfigList(listOf(entry)) else YamlProxyTargetConfigList(emptyList())
                }
                else -> YamlProxyTargetConfigList(emptyList())
            }
        }

        private fun JsonNode.toConfigOrNull(): YamlProxyTargetConfig? {
            return try {
                objectMapper.treeToValue(this, YamlProxyTargetConfig::class.java)
            } catch (_: Exception) {
                null
            }
        }
    }
}
