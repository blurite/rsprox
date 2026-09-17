package net.rsprox.proxy.rs3.relay

import java.io.ByteArrayOutputStream
import java.util.concurrent.CompletableFuture

/** Keeps intervening packets in order and reuses exactly the original world-list packet count. */
internal class Rs3WorldListRewrite(
    private val rewriter: Rs3EndpointRewriter,
) {
    private val pending = ArrayList<Rs3WirePacket>()
    private val data = ByteArrayOutputStream()
    private var heldBytes = 0
    private var definitionsKnown = false
    val isPending: Boolean get() = pending.isNotEmpty()

    fun accept(packet: Rs3WirePacket): CompletableFuture<List<ByteArray>> {
        val isList = packet.entry.name == "WORLDLIST_FETCH_REPLY"
        if (!isList && !isPending) return rewriteOrdinary(packet).thenApply { listOf(it) }
        pending += packet
        heldBytes += packet.payload.size + 4
        require(heldBytes <= 2 * 1024 * 1024) { "Too much traffic held behind incomplete world list" }
        if (packet.entry.name in LOGOUT_PACKETS) return interrupt()
        if (!isList) return CompletableFuture.completedFuture(emptyList())
        require(packet.payload.isNotEmpty()) { "World-list chunk has no completion flag" }
        data.write(packet.payload, 1, packet.payload.size - 1)
        require(data.size() <= 20_000) { "World list exceeds native accumulator" }
        if (packet.payload[0] != 1.toByte()) return CompletableFuture.completedFuture(emptyList())
        val original = data.toByteArray()
        val frames = pending.toList()
        pending.clear()
        data.reset()
        heldBytes = 0
        return rewriter.worldList(original, definitionsKnown).thenCompose { rewritten ->
            if (original[1] == 1.toByte()) definitionsKnown = true
            val last = frames.indexOfLast { it.entry.name == "WORLDLIST_FETCH_REPLY" }
            var offset = 0
            val results =
                frames.mapIndexed { index, frame ->
                    if (frame.entry.name != "WORLDLIST_FETCH_REPLY") {
                        rewriteOrdinary(frame)
                    } else {
                        val size =
                            if (index == last) {
                                rewritten.size - offset
                            } else {
                                minOf(frame.payload.size - 1, rewritten.size - offset)
                            }
                        val body = ByteArray(size + 1)
                        body[0] = if (index == last) 1 else 0
                        rewritten.copyInto(body, 1, offset, offset + size)
                        offset += size
                        CompletableFuture.completedFuture(frame.encode(body))
                    }
                }
            CompletableFuture.allOf(*results.toTypedArray()).thenApply { results.map { it.join() } }
        }
    }

    private fun interrupt(): CompletableFuture<List<ByteArray>> {
        val frames = pending.toList()
        pending.clear()
        data.reset()
        heldBytes = 0
        var output = CompletableFuture.completedFuture(emptyList<ByteArray>())
        for (frame in frames) {
            output =
                output.thenCompose { earlier ->
                    val rewritten =
                        if (frame.entry.name == "WORLDLIST_FETCH_REPLY") {
                            // Keep each encrypted opcode in place: dropping a packet shifts ISAAC.
                            // Native 950 accepts a completion=0 chunk with no data. Nothing from
                            // this unfinished list has been forwarded, so empty chunks retain the
                            // previous world definitions without exposing unrewritten hostnames.
                            CompletableFuture.completedFuture(frame.encode(byteArrayOf(0)))
                        } else {
                            rewriteOrdinary(frame)
                        }
                    rewritten.thenApply { earlier + it }
                }
        }
        return output
    }

    private fun rewriteOrdinary(packet: Rs3WirePacket): CompletableFuture<ByteArray> {
        return when (packet.entry.name) {
            "CHANGE_LOBBY", "LOGOUT_TRANSFER" ->
                rewriter
                    .redirect(
                        packet.entry.name,
                        packet.payload,
                    ).thenApply(packet::encode)
            else -> CompletableFuture.completedFuture(packet.encode())
        }
    }

    private companion object {
        private val LOGOUT_PACKETS = setOf("LOGOUT", "LOGOUT_FULL", "LOGOUT_TRANSFER")
    }
}
