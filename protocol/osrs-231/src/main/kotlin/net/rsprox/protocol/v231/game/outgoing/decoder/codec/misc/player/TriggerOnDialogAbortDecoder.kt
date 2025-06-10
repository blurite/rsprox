package net.rsprox.protocol.v231.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.TriggerOnDialogAbort
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class TriggerOnDialogAbortDecoder : ProxyMessageDecoder<TriggerOnDialogAbort> {
    override val prot: ClientProt = GameServerProt.TRIGGER_ONDIALOGABORT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): TriggerOnDialogAbort {
        return TriggerOnDialogAbort
    }
}
