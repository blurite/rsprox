package net.rsprox.protocol.v224.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.v224.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.client.MapBuildComplete
import net.rsprox.protocol.session.Session

@Consistent
public class MapBuildCompleteDecoder : ProxyMessageDecoder<MapBuildComplete> {
    override val prot: ClientProt = GameClientProt.MAP_BUILD_COMPLETE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapBuildComplete = MapBuildComplete
}
