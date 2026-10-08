package net.rsprox.gui

internal const val MCP_ENDPOINT_TOOLTIP: String =
    "No password: any program on this computer can read every session's packets, chat included. " +
        "Applies after a restart."

/**
 * The endpoint starts and stops only with RSProx, so a switch away from what this run started with
 * takes effect at the next start. Returns what to tell the user then, and null when nothing is pending.
 */
internal fun mcpRestartNotice(
    enabledAtStart: Boolean,
    enabled: Boolean,
    port: Int,
): String? =
    when {
        enabled == enabledAtStart -> null
        enabled ->
            "RSProx serves the MCP endpoint on http://127.0.0.1:$port/mcp after a restart.\n\n" +
                "The endpoint has no password. Any program on this computer can read the packets of every " +
                "session through it, chat included."
        else -> "RSProx keeps serving the MCP endpoint until it is restarted."
    }
