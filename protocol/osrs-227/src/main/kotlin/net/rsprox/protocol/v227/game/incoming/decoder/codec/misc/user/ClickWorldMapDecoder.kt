package net.rsprox.protocol.v227.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.incoming.model.misc.user.ClickWorldMap
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

internal class ClickWorldMapDecoder : ProxyMessageDecoder<ClickWorldMap> {
    override val prot: ClientProt = GameClientProt.CLICKWORLDMAP

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClickWorldMap {
        val packed = buffer.g4Alt2()
        return ClickWorldMap(CoordGrid(packed))
    }
}
