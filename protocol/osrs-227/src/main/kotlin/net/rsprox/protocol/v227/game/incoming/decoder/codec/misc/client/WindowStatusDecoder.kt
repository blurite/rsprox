package net.rsprox.protocol.v227.game.incoming.decoder.codec.misc.client
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.client.WindowStatus
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class WindowStatusDecoder : ProxyMessageDecoder<WindowStatus> {
    override val prot: ClientProt = GameClientProt.WINDOW_STATUS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): WindowStatus {
        val windowMode = buffer.g1()
        val frameWidth = buffer.g2()
        val frameHeight = buffer.g2()
        return WindowStatus(
            windowMode,
            frameWidth,
            frameHeight,
        )
    }
}
