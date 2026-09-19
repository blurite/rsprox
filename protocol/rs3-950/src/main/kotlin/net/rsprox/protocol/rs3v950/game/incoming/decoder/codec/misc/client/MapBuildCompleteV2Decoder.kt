package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.client.MapBuildCompleteV2
import net.rsprox.protocol.session.Session

internal class MapBuildCompleteV2Decoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<MapBuildCompleteV2> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): MapBuildCompleteV2 {
        val buildDurationMillis = buffer.g4()
        return MapBuildCompleteV2(
            buildDurationMillis,
        )
    }
}
