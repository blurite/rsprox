package net.rsprox.protocol.v236.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.common.CoordGrid
import net.rsprox.protocol.game.outgoing.model.misc.player.SetMapFlagV2
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt

internal class SetMapFlagV2Decoder : ProxyMessageDecoder<SetMapFlagV2> {
    override val prot: ClientProt = GameServerProt.SET_MAP_FLAG_V2

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetMapFlagV2 {
        val coordGrid = CoordGrid(buffer.g4())
        return SetMapFlagV2(coordGrid)
    }
}
