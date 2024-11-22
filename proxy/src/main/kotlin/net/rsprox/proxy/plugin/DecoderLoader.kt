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
import net.rsprox.proxy.huffman.HuffmanProvider
import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import kotlin.time.measureTimedValue

public class DecoderLoader {
    private val decoders: MutableMap<Int, RevisionDecoder> = mutableMapOf()

    public fun load(
        cache: CacheProvider,
        latestOnly: Boolean = false,
    ) {
        if (decoders.isNotEmpty()) return
        val huffmanCodec = HuffmanProvider.get()
        val pool = ForkJoinPool.commonPool()
        val tasks = mutableListOf<Callable<RevisionDecoder>>()
        // Load the classes in parallel here to speed up the process, especially over time as we
        // get more and more modules; there are about 230 classes per module, and our JDK supports
        // parallel class-loading so it significantly speeds the process up.
        if (latestOnly) {
            loadLatestRevision(tasks, huffmanCodec, cache)
        } else {
            loadAllRevisions(tasks, huffmanCodec, cache)
        }
        val (results, time) =
            measureTimedValue {
                pool.invokeAll(tasks)
            }
        logger.debug { "Finished loading decoders in $time" }
        for (result in results) {
            val plugin = result.get()
            decoders[plugin.revision] = plugin
        }
    }

    private fun loadLatestRevision(
        tasks: MutableList<Callable<RevisionDecoder>>,
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ) {
        tasks +=
            Callable {
                loadRevision227(huffmanCodec, cache)
            }
    }

    private fun loadAllRevisions(
        tasks: MutableList<Callable<RevisionDecoder>>,
        huffmanCodec: HuffmanCodec,
        cache: CacheProvider,
    ) {
        tasks +=
            Callable {
                loadRevision223(huffmanCodec, cache)
            }
        tasks +=
            Callable {
                loadRevision224(huffmanCodec, cache)
            }
        tasks +=
            Callable {
                loadRevision225(huffmanCodec, cache)
            }
        tasks +=
            Callable {
                loadRevision226(huffmanCodec, cache)
            }
        loadLatestRevision(tasks, huffmanCodec, cache)
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
