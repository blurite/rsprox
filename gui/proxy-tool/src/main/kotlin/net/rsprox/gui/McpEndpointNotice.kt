package net.rsprox.gui

internal const val MCP_ENDPOINT_TOOLTIP: String =
    "No password: any program on this computer can read every session's packets, chat included. " +
        "While off, nothing is served; sessions of clients launched in this run are kept in memory and " +
        "listed again when it is turned back on."

internal data class McpEndpointSwitch(
    val served: Boolean,
    val failure: String?,
)

/**
 * Whether the item starts ticked. An endpoint that is on was served at start-up, unless its port was
 * taken then, so it is asked once more. The saved choice is left alone, so the next start tries again.
 */
internal fun isMcpEndpointServed(
    enabled: Boolean,
    serve: () -> String?,
): Boolean = enabled && serve() == null

/**
 * Turns the endpoint on or off at once, and saves the choice once it holds. An endpoint that cannot be
 * served stays off with nothing saved, and the result says why.
 */
internal fun switchMcpEndpoint(
    enabled: Boolean,
    serve: () -> String?,
    stopServing: () -> Unit,
    save: (Boolean) -> Unit,
): McpEndpointSwitch {
    if (!enabled) {
        stopServing()
        save(false)
        return McpEndpointSwitch(served = false, failure = null)
    }
    val refusal = serve()
    if (refusal != null) {
        return McpEndpointSwitch(served = false, failure = refusal.replace(". ", ".\n"))
    }
    save(true)
    return McpEndpointSwitch(served = true, failure = null)
}
