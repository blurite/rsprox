package net.rsprox.protocol.v226.game.incoming.decoder.codec.misc.user
import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.SetHeading
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

internal class SetHeadingDecoder : ProxyMessageDecoder<SetHeading> {
    override val prot: ClientProt = GameClientProt.SET_HEADING

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetHeading {
        val heading = buffer.g1Alt2()
        return SetHeading(heading)
    }
}
