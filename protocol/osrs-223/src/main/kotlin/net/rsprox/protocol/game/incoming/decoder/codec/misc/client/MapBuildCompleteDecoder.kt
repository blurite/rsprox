package net.rsprox.protocol.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.MapBuildComplete

@Consistent
public class MapBuildCompleteDecoder : MessageDecoder<MapBuildComplete> {
    override val prot: ClientProt = GameClientProt.MAP_BUILD_COMPLETE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): MapBuildComplete {
        return MapBuildComplete
    }
}
