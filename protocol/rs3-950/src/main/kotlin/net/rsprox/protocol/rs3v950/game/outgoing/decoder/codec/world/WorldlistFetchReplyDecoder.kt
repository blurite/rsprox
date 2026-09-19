package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.world

import io.netty.buffer.Unpooled
import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toJagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply.Country
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply.Definitions
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply.MarkedString
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply.Population
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply.Reply
import net.rsprox.protocol.rs3.game.outgoing.model.world.WorldlistFetchReply.World
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.session.attribute

internal class WorldlistFetchReplyDecoder : ProxyMessageDecoder<WorldlistFetchReply> {
    override val prot: ClientProt = GameServerProt.WORLDLIST_FETCH_REPLY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): WorldlistFetchReply {
        val completeFlag = buffer.g1()
        val previous = session.pendingWorldList ?: byteArrayOf()
        val chunkLength = buffer.readableBytes()
        require(previous.size + chunkLength <= 20_000) {
            "World list exceeds the native 20000-byte accumulator; refusing to silently discard its tail"
        }
        val accumulated = previous.copyOf(previous.size + chunkLength)
        // This is the protocol's chunk payload, not a snapshot of already decoded packet bytes.
        buffer.buffer.readBytes(accumulated, previous.size, chunkLength)
        if (completeFlag != 1) {
            session.pendingWorldList = accumulated
            return WorldlistFetchReply(completeFlag, chunkLength, accumulated.size, null)
        }
        session.pendingWorldList = null
        val input = Unpooled.wrappedBuffer(accumulated)
        try {
            val data = input.toJagByteBuf()
            val version = data.g1()
            require(version == 2) { "Unsupported world-list version $version" }
            val definitionsFlag = data.g1()
            val definitions = if (definitionsFlag == 1) readDefinitions(data) else null
            val worldCount = definitions?.worlds?.size ?: session.worldListCount
            // The client can already have definitions from before this proxy session.
            // A completed reply still frames its population records; keep their relative indices.
            val populations =
                buildList {
                    while (data.readableBytes() > 0 && (worldCount == null || size < worldCount)) {
                        require(data.readableBytes() >= 3) { "Truncated world population record" }
                        val index = data.gSmart1or2()
                        require(data.readableBytes() >= 2) { "Truncated world population value" }
                        val population = data.g2()
                        add(Population(index, if (population == 65535) -1 else population))
                    }
                }
            require(worldCount == null || populations.size == worldCount) { "Truncated world populations" }
            require(data.readableBytes() == 0) { "Unexpected bytes after world list" }
            session.worldListCount = worldCount
            return WorldlistFetchReply(
                completeFlag,
                chunkLength,
                accumulated.size,
                Reply(version, definitionsFlag, definitions, populations),
            )
        } finally {
            input.release()
        }
    }

    private fun readDefinitions(buffer: JagByteBuf): Definitions {
        val countryCount = buffer.gSmart1or2()
        require(countryCount <= buffer.readableBytes() / 2) { "Truncated world countries" }
        val countries = List(countryCount) { Country(buffer.gSmart1or2(), readString(buffer)) }
        val minimumWorldId = buffer.gSmart1or2()
        val maximumWorldId = buffer.gSmart1or2()
        val worldCount = buffer.gSmart1or2()
        require(worldCount <= buffer.readableBytes() / 9) { "Truncated world definitions" }
        val worlds =
            List(worldCount) {
                val index = buffer.gSmart1or2()
                val countryIndex = buffer.g1()
                val properties = buffer.g4()
                val extraId = buffer.gSmart1or2()
                val extraName = if (extraId != 0) readString(buffer) else null
                val activity = readString(buffer)
                val hostname = readString(buffer)
                World(index, countryIndex, properties, extraId, extraName, activity, hostname)
            }
        return Definitions(countries, minimumWorldId, maximumWorldId, worlds, buffer.g4())
    }

    private fun readString(buffer: JagByteBuf): MarkedString {
        val marker = buffer.g1()
        return MarkedString(marker, if (marker == 0) buffer.readNativeString() else null)
    }

    private companion object {
        private var Session.pendingWorldList: ByteArray? by attribute()
        private var Session.worldListCount: Int? by attribute()
    }
}
