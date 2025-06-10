package net.rsprox.protocol.v231.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.OculusLeave
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.incoming.decoder.prot.GameClientProt

@Consistent
public class OculusLeaveDecoder : ProxyMessageDecoder<OculusLeave> {
    override val prot: ClientProt = GameClientProt.OCULUS_LEAVE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OculusLeave = OculusLeave
}
