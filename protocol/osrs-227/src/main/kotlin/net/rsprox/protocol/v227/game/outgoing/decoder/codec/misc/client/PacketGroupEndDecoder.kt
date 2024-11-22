package net.rsprox.protocol.v227.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.PacketGroupEnd
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class PacketGroupEndDecoder : ProxyMessageDecoder<PacketGroupEnd> {
    override val prot: ClientProt = GameServerProt.PACKET_GROUP_END

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PacketGroupEnd {
        return PacketGroupEnd(buffer.g2())
    }
}
