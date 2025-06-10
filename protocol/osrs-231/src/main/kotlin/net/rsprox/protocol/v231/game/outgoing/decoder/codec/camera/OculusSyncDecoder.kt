package net.rsprox.protocol.v231.game.outgoing.decoder.codec.camera

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.camera.OculusSync
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class OculusSyncDecoder : ProxyMessageDecoder<OculusSync> {
    override val prot: ClientProt = GameServerProt.OCULUS_SYNC

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): OculusSync {
        val value = buffer.g4()
        return OculusSync(value)
    }
}
