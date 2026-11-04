package net.rsprox.mcp.server

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import net.rsprox.mcp.packets.Cursor
import net.rsprox.mcp.packets.Origin
import net.rsprox.mcp.packets.PacketLog
import net.rsprox.mcp.packets.PacketQuery
import net.rsprox.mcp.packets.PacketRecord
import net.rsprox.mcp.session.Session
import java.util.concurrent.TimeUnit

/**
 * The prot prefixes that prove an interaction, for each expected prefix that is not its own only proof.
 * A widget op on a dialog option runs the widget's script, which sends RESUME_PAUSEBUTTON and no button
 * packet, and a close button sends CLOSE_MODAL.
 */
private val PROVING_PREFIXES =
    mapOf("IF_BUTTON" to setOf("IF_BUTTON", "IF_SUBOP", "RESUME_PAUSEBUTTON", "CLOSE_MODAL"))

/** The prot prefix of the record that the client sends for every press of a mouse button. */
private const val MOUSE_CLICK = "EVENT_MOUSE_CLICK"

/** The canvas position in the text of a mouse click record. */
private val CLICK_POINT = Regex("""\bx=(\d+), y=(\d+)\b""")

/** The note on an interaction whose login has no decoder, so that none of its packets reach the log. */
private const val NOT_DECODED =
    "the packets of this login are not decoded, so the action could not be confirmed in the packet log"

/** The note on a widget interaction that the client performed without a packet. */
private const val NO_PACKET =
    "the client performed the option and sent no packet for it, which is normal for an option the client " +
        "handles by itself"

/**
 * Add the packet that the client sent for an interaction to the plugin's [answer], as `packet`.
 * The plugin names the prot prefix to expect and the canvas point it pressed, and neither is part of
 * the result. Packets reach the log up to a tick late, so the packet is tied to this interaction by
 * the client's own record of the press: it is the first packet with a proving prefix after that
 * record, and `proof` is then `click`. When no such record arrives within [waitMs], the packet is the
 * first one with a proving prefix after the answer's `cursor`, and `proof` is `prefix`.
 *
 * Throws [ToolError] when the client sent no packet for a target in the game world. A widget option
 * that the client handles by itself sends none, so `packet` is then null and `note` says why.
 */
internal fun confirmSent(
    session: Session,
    answer: ObjectNode,
    waitMs: Long,
): ObjectNode {
    val expect = answer.remove("expect").asText()
    val pressed = point(answer.remove("pressed"))

    if (session.logins.current()?.transcribing != true) return answer.putNull("packet").put("note", NOT_DECODED)

    val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(waitMs)
    val cursor = Cursor(answer.get("cursor").asLong())
    val click = awaitClick(session.packets, cursor, pressed, deadline)
    val query =
        PacketQuery(
            after = if (click == null) cursor else Cursor(click.seq),
            origin = Origin.CLIENT,
            limit = 1,
            protPrefixes = PROVING_PREFIXES[expect] ?: setOf(expect),
        )

    val sent = session.packets.read(query, remainingMs(deadline)).packets.firstOrNull()
    if (sent == null) return unsent(answer, expect, waitMs)

    return answer.put("packet", line(sent)).put("proof", if (click == null) "prefix" else "click")
}

/**
 * Find the client's record of the press at [pressed] among the mouse clicks after [after].
 * Returns null when none arrives before [deadline].
 */
private fun awaitClick(
    packets: PacketLog,
    after: Cursor,
    pressed: Pair<Int, Int>,
    deadline: Long,
): PacketRecord? {
    var cursor = after

    while (true) {
        val query = PacketQuery(after = cursor, origin = Origin.CLIENT, protPrefixes = setOf(MOUSE_CLICK))
        val page = packets.read(query, remainingMs(deadline))
        val click = page.packets.firstOrNull { clickPoint(it.text) == pressed }

        if (click != null || page.packets.isEmpty()) return click

        cursor = page.next
    }
}

/**
 * Get the canvas position in the text of a mouse click record, or null when the text holds none.
 * The transcriber prints the record as
 * `[event_mouse_click_v2] lasttransmitted=40ms, x=312, y=171, rightclick=false, code=0`.
 */
private fun clickPoint(text: String): Pair<Int, Int>? {
    val (x, y) = CLICK_POINT.find(text)?.destructured ?: return null

    return x.toInt() to y.toInt()
}

/** Get the canvas point that the plugin answered as an array of its x and its y. */
private fun point(pressed: JsonNode): Pair<Int, Int> = pressed.get(0).asInt() to pressed.get(1).asInt()

/** Get the milliseconds that are left until [deadline], or 0 once it has passed. */
private fun remainingMs(deadline: Long): Long =
    TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()).coerceAtLeast(0)

/**
 * Answer an interaction that the client sent no packet for. Throws [ToolError] for a target in the
 * game world, where the client dropped the action.
 */
private fun unsent(
    answer: ObjectNode,
    expect: String,
    waitMs: Long,
): ObjectNode {
    val kind = TargetKind.of(answer.get("target").asText())
    if (!kind.inWorld) return answer.putNull("packet").put("note", NO_PACKET)

    val dropped = "the client sent no $expect packet within $waitMs ms, so the action was dropped"

    throw ToolError("$dropped. Likely cause: ${kind.dropCause}.")
}
