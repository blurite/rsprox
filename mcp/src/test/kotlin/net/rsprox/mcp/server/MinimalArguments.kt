package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode

/** The smallest arguments [tool] accepts: a value of the right type for each required property. */
internal fun minimalArguments(tool: Tool): ObjectNode {
    val arguments = McpDispatcher.MAPPER.createObjectNode()

    for (required in tool.inputSchema.get("required") ?: emptyList<JsonNode>()) {
        val property = tool.inputSchema.get("properties").get(required.asText())

        when (val type = property.get("type").asText()) {
            "string" -> arguments.put(required.asText(), property.get("enum")?.get(0)?.asText() ?: "x")
            "integer" -> arguments.put(required.asText(), property.get("minimum")?.asLong() ?: 0)
            "boolean" -> arguments.put(required.asText(), false)
            "array" -> arguments.putArray(required.asText())
            else -> error("${tool.name} requires '${required.asText()}' of type $type, which this helper cannot make")
        }
    }

    return arguments
}
