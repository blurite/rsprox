package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3v949.game.outgoing.model.interfaces.IfSetEvents
import net.rsprox.protocol.session.Session

internal class IfSetEventsDecoder : ProxyMessageDecoder<IfSetEvents> {
    override val prot: ClientProt = GameServerProt.IF_SETEVENTS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetEvents {
        val componentHash = buffer.g4Alt1().toLong() and 0xFFFFFFFFL
        val fromSlot = buffer.g2()
        val settings = buffer.g4Alt3()
        val toSlot = buffer.g2Alt1()
        return IfSetEvents(
            componentHash,
            fromSlot,
            toSlot,
            settings,
        )
    }
}
