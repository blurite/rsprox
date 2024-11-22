package net.rsprox.protocol.v227.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.PacketGroupStart
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class PacketGroupStartDecoder : ProxyMessageDecoder<PacketGroupStart> {
    override val prot: ClientProt = GameServerProt.PACKET_GROUP_START

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): PacketGroupStart {
        val length = buffer.g2s()
        return PacketGroupStart(length)
    }
}
