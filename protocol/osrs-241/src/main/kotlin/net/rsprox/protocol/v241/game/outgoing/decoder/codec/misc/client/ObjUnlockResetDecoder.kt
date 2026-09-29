package net.rsprox.protocol.v241.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.ObjUnlockReset
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

internal class ObjUnlockResetDecoder : ProxyMessageDecoder<ObjUnlockReset> {
    override val prot: ClientProt = GameServerProt.OBJUNLOCK_RESET

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjUnlockReset {
        return ObjUnlockReset
    }
}
