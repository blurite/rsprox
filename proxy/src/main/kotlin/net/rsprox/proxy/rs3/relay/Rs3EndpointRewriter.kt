package net.rsprox.proxy.rs3.relay

import net.rsprox.proxy.rs3.binary.Rs3BinaryRecorder.WorldMetadata
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.concurrent.CompletableFuture

/** Revision-950 endpoint payloads. All bytes outside hostname spans are retained verbatim. */
internal class Rs3EndpointRewriter(
    private val addresses: Rs3LocalAddressSpace,
    private val register: (List<Rs3RelayRoute>) -> CompletableFuture<Unit>,
    private val worldPorts: List<Int>,
    private val onWorldDefinitions: (Map<Int, WorldMetadata>) -> Unit = {},
) {
    private data class Host(
        val start: Int,
        val end: Int,
        val value: String,
        val marked: Boolean,
    )

    private data class Replacement(
        val host: Host,
        val endpoint: Rs3Endpoint,
        val ports: List<Int>,
    )

    fun lobbySuccess(payload: ByteArray): CompletableFuture<ByteArray> {
        val reader = Reader(payload)
        if (reader.u1() == 1) reader.skip(4)
        // Account fields before the display-name string; see FUN_002db7a0.
        reader.skip(47)
        reader.string(marked = true)
        reader.skip(5)
        val world = reader.u2()
        val host = reader.string(marked = true)
        val ports = listOf(reader.u2(), reader.u2())
        reader.skip(16) // Two session values, preserved without interpretation.
        reader.end()
        if (world == 65535) return CompletableFuture.completedFuture(payload)
        return replace(payload, listOf(Replacement(host, Rs3Endpoint.World(world), ports)), 255)
    }

    fun redirect(
        name: String,
        payload: ByteArray,
    ): CompletableFuture<ByteArray> {
        val reader = Reader(payload)
        val endpoint: Rs3Endpoint
        val host: Host
        when (name) {
            "CHANGE_LOBBY" -> {
                host = reader.string(marked = false)
                endpoint = Rs3Endpoint.Lobby(reader.u2())
            }
            "LOGOUT_TRANSFER" -> {
                endpoint = Rs3Endpoint.World(reader.u2())
                host = reader.string(marked = false)
            }
            else -> error("Not an endpoint redirect: $name")
        }
        val ports = listOf(reader.u2(), reader.u2())
        if (name == "LOGOUT_TRANSFER") reader.u1()
        reader.end()
        return replace(payload, listOf(Replacement(host, endpoint, ports)), 255)
    }

    fun worldList(
        payload: ByteArray,
        definitionsKnown: Boolean,
    ): CompletableFuture<ByteArray> {
        require(payload.size <= 20_000) { "World list exceeds native accumulator" }
        val reader = Reader(payload)
        require(reader.u1() == 2) { "Unsupported world-list format" }
        val replacements = ArrayList<Replacement>()
        var definitions: Map<Int, WorldMetadata>? = null
        var worldCount: Int? = null
        if (reader.u1() == 1) {
            val countries =
                List(reader.smart()) {
                    val id = reader.smart()
                    reader.string(marked = true)
                    id
                }
            val minimum = reader.smart()
            val maximum = reader.smart()
            require(minimum <= maximum) { "Invalid world-list range" }
            worldCount = reader.smart()
            val ids = HashSet<Int>()
            val worlds = HashMap<Int, WorldMetadata>()
            repeat(worldCount) {
                val id = minimum + reader.smart()
                require(id in minimum..maximum && ids.add(id)) { "Invalid or duplicate world id" }
                val countryIndex = reader.u1()
                val location = countries.getOrNull(countryIndex)
                require(location != null) { "Invalid world country index" }
                val flags = reader.u4()
                if (reader.smart() != 0) reader.string(marked = true)
                val activity = reader.string(marked = true).value
                worlds[id] = WorldMetadata(flags, location, activity)
                replacements += Replacement(reader.string(marked = true), Rs3Endpoint.World(id), worldPorts)
            }
            definitions = worlds
            reader.skip(4) // Opaque definitions token: native retains it, it does not recalculate it.
        } else {
            require(definitionsKnown) { "Population-only reply before mapped world definitions" }
        }
        var populations = 0
        while (reader.remaining > 0) {
            reader.smart()
            reader.u2()
            populations++
        }
        require(worldCount == null || populations == worldCount) { "Incomplete world populations" }
        return replace(payload, replacements, 20_000).thenApply { rewritten ->
            // Publish only complete, validated definitions, before forwarding them to the client.
            // The recorder queues this before a subsequent game login on any relay connection.
            definitions?.let(onWorldDefinitions)
            rewritten
        }
    }

    private fun replace(
        payload: ByteArray,
        replacements: List<Replacement>,
        limit: Int,
    ): CompletableFuture<ByteArray> {
        // Validate the entire payload/result before binding anything.
        val routes = ArrayList<Rs3RelayRoute>()
        val output = ByteArrayOutputStream()
        var position = 0
        for (replacement in replacements) {
            val host = replacement.host
            require(host.value.isNotEmpty()) { "Missing destination hostname" }
            output.write(payload, position, host.start - position)
            if (host.marked) output.write(0)
            output.write(addresses.address(replacement.endpoint).hostAddress.toByteArray(Charsets.US_ASCII))
            output.write(0)
            position = host.end
            for (port in replacement.ports.distinct()) {
                routes += Rs3RelayRoute(addresses, replacement.endpoint, host.value, port)
            }
        }
        output.write(payload, position, payload.size - position)
        val result = output.toByteArray()
        require(result.size <= limit) { "Rewritten endpoint payload exceeds native length limit" }
        return register(routes).thenApply { result }
    }

    private class Reader(
        private val data: ByteArray,
    ) {
        private var position = 0
        val remaining: Int get() = data.size - position

        fun skip(count: Int) {
            require(count >= 0 && count <= remaining) { "Truncated endpoint payload" }
            position += count
        }

        fun u1(): Int {
            require(remaining > 0) { "Truncated endpoint payload" }
            return data[position++].toInt() and 255
        }

        fun u2(): Int = (u1() shl 8) or u1()

        fun u4(): Int = (u2() shl 16) or u2()

        fun smart(): Int {
            val first = u1()
            return if (first < 128) first else ((first - 128) shl 8) or u1()
        }

        fun string(marked: Boolean): Host {
            val start = position
            if (marked && u1() != 0) return Host(start, position, "", true)
            val textStart = position
            while (u1() != 0) { /* Bounds checked by u1. */ }
            // Native strings are CP1252; undefined extension bytes are dropped.
            val value = String(data, textStart, position - textStart - 1, CP1252).replace("\uFFFD", "")
            return Host(start, position, value, marked)
        }

        fun end() {
            require(remaining == 0) { "Unexpected trailing endpoint fields" }
        }
    }

    private companion object {
        val CP1252: Charset = Charset.forName("windows-1252")
    }
}
