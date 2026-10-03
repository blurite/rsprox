package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.node.ObjectNode
import net.rsprox.mcp.packets.Cursor
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.PacketLog
import net.rsprox.mcp.packets.PacketQuery

/**
 * The prot prefixes that prove an interaction, for each expected prefix that is not its own only proof.
 * A widget op on a dialog option runs the widget's script, which sends RESUME_PAUSEBUTTON and no button packet.
 */
private val PROVING_PREFIXES = mapOf("IF_BUTTON" to setOf("IF_BUTTON", "IF_SUBOP", "RESUME_PAUSEBUTTON"))

/** The likely cause of a dropped interaction, by the kind of target the client drops silently. */
private val DROP_CAUSES =
    mapOf(
        "npc" to "the NPC left the client's view",
        "player" to "the player left the client's view",
        "widget" to "the server has not enabled that op on the widget",
    )

/** The likely cause of a dropped interaction with a target that the client sends a packet for unchecked. */
private const val DROP_CAUSE = "the client logged out or lost its connection"

/**
 * Add the packet that the client sent for an interaction to the plugin's [answer], as `packet`.
 * The packet is the first one from the client after the answer's `cursor` whose prot starts with the
 * answer's `expect`. Throws [ToolError] when none arrives within [waitMs], or when a walk went to
 * another tile than the one asked for. An answer without `expect` names nothing to wait for.
 */
internal fun confirmSent(
    packets: PacketLog,
    answer: ObjectNode,
    waitMs: Long,
): ObjectNode {
    val expect = answer.get("expect")?.asText() ?: return answer
    val query =
        PacketQuery(
            after = Cursor(answer.get("cursor").asLong()),
            origin = Origin.CLIENT,
            limit = 1,
            protPrefixes = PROVING_PREFIXES[expect] ?: setOf(expect),
        )

    val sent = packets.read(query, waitMs).packets.firstOrNull() ?: throw ToolError(dropped(answer, expect, waitMs))
    val tile = "coord=(${answer.get("x")}, ${answer.get("y")}, "
    val elsewhere = expect == "MOVE_GAMECLICK" && tile !in sent.text

    if (elsewhere) throw ToolError("the client walked to another tile than the one asked for: ${line(sent)}")

    return answer.put("packet", line(sent))
}

/** Build the message for an interaction that the client sent no packet for. */
private fun dropped(
    answer: ObjectNode,
    expect: String,
    waitMs: Long,
): String {
    val cause = DROP_CAUSES[answer.get("target")?.asText()] ?: DROP_CAUSE

    return "the client sent no $expect packet within $waitMs ms, so the action was dropped. Likely cause: $cause."
}
