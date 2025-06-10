package net.rsprox.proxy.plugin

import com.github.michaelbull.logging.InlineLogger
import net.rsprot.compression.HuffmanCodec
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.v223.ClientPacketDecoderServiceV223
import net.rsprox.protocol.v223.GameClientProtProviderV223
import net.rsprox.protocol.v223.GameServerProtProviderV223
import net.rsprox.protocol.v223.ServerPacketDecoderServiceV223
import net.rsprox.protocol.v224.ClientPacketDecoderServiceV224
import net.rsprox.protocol.v224.GameClientProtProviderV224
import net.rsprox.protocol.v224.GameServerProtProviderV224
import net.rsprox.protocol.v224.ServerPacketDecoderServiceV224
import net.rsprox.protocol.v225.ClientPacketDecoderServiceV225
import net.rsprox.protocol.v225.GameClientProtProviderV225
import net.rsprox.protocol.v225.GameServerProtProviderV225
import net.rsprox.protocol.v225.ServerPacketDecoderServiceV225
import net.rsprox.protocol.v226.ClientPacketDecoderServiceV226
import net.rsprox.protocol.v226.GameClientProtProviderV226
import net.rsprox.protocol.v226.GameServerProtProviderV226
import net.rsprox.protocol.v226.ServerPacketDecoderServiceV226
import net.rsprox.protocol.v227.ClientPacketDecoderServiceV227
import net.rsprox.protocol.v227.GameClientProtProviderV227
import net.rsprox.protocol.v227.GameServerProtProviderV227
import net.rsprox.protocol.v227.ServerPacketDecoderServiceV227
import net.rsprox.protocol.v228.ClientPacketDecoderServiceV228
import net.rsprox.protocol.v228.GameClientProtProviderV228
import net.rsprox.protocol.v228.GameServerProtProviderV228
import net.rsprox.protocol.v228.ServerPacketDecoderServiceV228
import net.rsprox.protocol.v229.ClientPacketDecoderServiceV229
import net.rsprox.protocol.v229.GameClientProtProviderV229
import net.rsprox.protocol.v229.GameServerProtProviderV229
import net.rsprox.protocol.v229.ServerPacketDecoderServiceV229
import net.rsprox.protocol.v230.ClientPacketDecoderServiceV230
import net.rsprox.protocol.v230.GameClientProtProviderV230
import net.rsprox.protocol.v230.GameServerProtProviderV230
import net.rsprox.protocol.v230.ServerPacketDecoderServiceV230
import net.rsprox.protocol.v231.ClientPacketDecoderServiceV231
import net.rsprox.protocol.v231.GameClientProtProviderV231
import net.rsprox.protocol.v231.GameServerProtProviderV231
import net.rsprox.protocol.v231.ServerPacketDecoderServiceV231
import net.rsprox.proxy.huffman.HuffmanProvider
import net.rsprox.transcriber.prot.GameClientProt
import net.rsprox.transcriber.prot.GameServerProt
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import kotlin.system.exitProcess
import kotlin.time.measureTimedValue

public class DecoderLoader {
    private val decoders: MutableMap<Int, RevisionDecoder> = mutableMapOf()

    public fun load(
        cache: CacheProvider,
        revision: Int? = null,
    ) {
        if (revision != null && revision in decoders) {
            return
        }
        val huffmanCodec = HuffmanProvider.get()
        val pool = ForkJoinPool.commonPool()
        val tasks = mutableListOf<Callable<RevisionDecoder>>()
        // Load the classes in parallel here to speed up the process, especially over time as we
        // get more and more modules; there are about 230 classes per module, and our JDK supports
        // parallel class-loading, so it significantly speeds the process up.
        val loadJobs = buildLoadJobs(huffmanCodec, cache)
        if (revision == null) {
            val missingJobs = loadJobs.filter { it.key !in decoders }
            tasks += missingJobs.values
        } else {
            tasks += loadJobs[revision] ?: error("Revision $revision decoder not found!")
        }
        if (tasks.isEmpty()) return
        val (results, time) =
            measureTimedValue {
                pool.invokeAll(tasks)
            }
        logger.debug { "Finished loading decoders in $time" }
        for (result in results) {
            val plugin = result.get()
            decoders[plugin.revision] = plugin
        }
        validateProtNames()
    }

    private fun validateProtNames() {
        var errorCount = 0
        for ((rev, decoder) in decoders) {
            for (serverProt in decoder.gameServerProtProvider.allProts()) {
                val prot = serverProtOrNull(serverProt.toString())
                if (prot == null) {
                    errorCount++
                    logger.error {
                        "Revision $rev defines invalid server prot: $serverProt"
                    }
                }
            }

            for (clientProt in decoder.gameClientProtProvider.allProts()) {
                val prot = clientProtOrNull(clientProt.toString())
                if (prot == null) {
                    errorCount++
                    logger.error {
                        "Revision $rev defines invalid client prot: $clientProt"
                    }
                }
            }
        }
        if (errorCount > 0) {
            logger.error {
                "Unable to proceed with binary decoding - invalid prots detected."
            }
            exitProcess(-1)
        }
    }

    private fun serverProtOrNull(name: String): GameServerProt? {
        return try {
            GameServerProt.valueOf(name)
        } catch (_: IllegalArgumentException) {
            return null
        }
    }

    private fun clientProtOrNull(name: String): GameClientProt? {
        return try {
            GameClientProt.valueOf(name)
        } catch (_: IllegalArgumentException) {
            return null
        }
    }

    private fun buildLoadJobs(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): Map<Int, Callable<RevisionDecoder>> {
        return mapOf(
            223 to Callable { loadRevision223(huffmanCodec, cache) },
            224 to Callable { loadRevision224(huffmanCodec, cache) },
            225 to Callable { loadRevision225(huffmanCodec, cache) },
            226 to Callable { loadRevision226(huffmanCodec, cache) },
            227 to Callable { loadRevision227(huffmanCodec, cache) },
            228 to Callable { loadRevision228(huffmanCodec, cache) },
            229 to Callable { loadRevision229(huffmanCodec, cache) },
            230 to Callable { loadRevision230(huffmanCodec, cache) },
            231 to Callable { loadRevision231(huffmanCodec, cache) },
        )
    }

    private fun loadRevision223(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 223 decoders" }
        return RevisionDecoder(
            223,
            ClientPacketDecoderServiceV223(huffmanCodec),
            ServerPacketDecoderServiceV223(huffmanCodec, cache),
            GameClientProtProviderV223,
            GameServerProtProviderV223,
        )
    }

    private fun loadRevision224(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 224 decoders" }
        return RevisionDecoder(
            224,
            ClientPacketDecoderServiceV224(huffmanCodec),
            ServerPacketDecoderServiceV224(huffmanCodec, cache),
            GameClientProtProviderV224,
            GameServerProtProviderV224,
        )
    }

    private fun loadRevision225(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 225 decoders" }
        return RevisionDecoder(
            225,
            ClientPacketDecoderServiceV225(huffmanCodec),
            ServerPacketDecoderServiceV225(huffmanCodec, cache),
            GameClientProtProviderV225,
            GameServerProtProviderV225,
        )
    }

    private fun loadRevision226(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 226 decoders" }
        return RevisionDecoder(
            226,
            ClientPacketDecoderServiceV226(huffmanCodec),
            ServerPacketDecoderServiceV226(huffmanCodec, cache),
            GameClientProtProviderV226,
            GameServerProtProviderV226,
        )
    }

    private fun loadRevision227(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 227 decoders" }
        return RevisionDecoder(
            227,
            ClientPacketDecoderServiceV227(huffmanCodec),
            ServerPacketDecoderServiceV227(huffmanCodec, cache),
            GameClientProtProviderV227,
            GameServerProtProviderV227,
        )
    }

    private fun loadRevision228(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 228 decoders" }
        return RevisionDecoder(
            228,
            ClientPacketDecoderServiceV228(huffmanCodec),
            ServerPacketDecoderServiceV228(huffmanCodec, cache),
            GameClientProtProviderV228,
            GameServerProtProviderV228,
        )
    }

    private fun loadRevision229(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 229 decoders" }
        return RevisionDecoder(
            229,
            ClientPacketDecoderServiceV229(huffmanCodec),
            ServerPacketDecoderServiceV229(huffmanCodec, cache),
            GameClientProtProviderV229,
            GameServerProtProviderV229,
        )
    }

    private fun loadRevision230(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 230 decoders" }
        return RevisionDecoder(
            230,
            ClientPacketDecoderServiceV230(huffmanCodec),
            ServerPacketDecoderServiceV230(huffmanCodec, cache),
            GameClientProtProviderV230,
            GameServerProtProviderV230,
        )
    }

    private fun loadRevision231(
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ): RevisionDecoder {
        logger.debug { "Loading revision 231 decoders" }
        return RevisionDecoder(
            231,
            ClientPacketDecoderServiceV231(huffmanCodec),
            ServerPacketDecoderServiceV231(huffmanCodec, cache),
            GameClientProtProviderV231,
            GameServerProtProviderV231,
        )
    }

    public fun getDecoder(revision: Int): RevisionDecoder {
        return decoders.getValue(revision)
    }

    public fun getDecoderOrNull(revision: Int): RevisionDecoder? {
        return decoders[revision]
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
