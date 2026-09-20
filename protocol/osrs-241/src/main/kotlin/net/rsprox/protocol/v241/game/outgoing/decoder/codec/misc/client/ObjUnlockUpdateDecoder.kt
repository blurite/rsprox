package net.rsprox.protocol.v241.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.client.ObjUnlockUpdate
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v241.game.outgoing.decoder.prot.GameServerProt

internal class ObjUnlockUpdateDecoder : ProxyMessageDecoder<ObjUnlockUpdate> {
    override val prot: ClientProt = GameServerProt.OBJUNLOCK_UPDATE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ObjUnlockUpdate {
        var wordIndex = 0
        val entries = buildList {
            while (buffer.isReadable) {
                wordIndex = Math.addExact(wordIndex, buffer.gSmart1or2())
                add(ObjUnlockUpdate.Entry(wordIndex, buffer.g8()))
            }
        }
        return ObjUnlockUpdate(entries)
    }
}
