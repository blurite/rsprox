package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.session.Session

internal class ClickWorldMapDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ClickWorldMap> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClickWorldMap {
        val packedCoordinate = buffer.g4Alt2()
        return ClickWorldMap(
            packedCoordinate,
        )
    }
}
