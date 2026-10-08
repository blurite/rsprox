package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.interfaces

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.interfaces.IfSetEvents
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class IfSetEventsDecoder : ProxyMessageDecoder<IfSetEvents> {
    override val prot: ClientProt = GameServerProt.IF_SETEVENTS

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfSetEvents {
        val componentHash = buffer.g4Alt3().toLong() and 0xFFFF_FFFFL
        val settings = buffer.g4()
        val toSlot = buffer.g2Alt2().let { if (it == 65535) -1 else it }
        val fromSlot = buffer.g2Alt2().let { if (it == 65535) -1 else it }
        return IfSetEvents(
            componentHash,
            fromSlot,
            toSlot,
            settings,
        )
    }
}
