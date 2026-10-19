package net.rsprox.protocol.rs3v951.game.outgoing.decoder.codec.selection

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.selection.SetLocOpOverride
import net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetLocOpOverrideDecoder : ProxyMessageDecoder<SetLocOpOverride> {
    override val prot: ClientProt = GameServerProt.SET_LOC_OP_OVERRIDE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetLocOpOverride {
        val overrideEnd = buffer.g4Alt2()
        val label = buffer.gjstr()
        val overrideStart = buffer.g4()
        val operation = buffer.g1Alt3()
        val rawCursor = buffer.g2Alt1()
        val cursor = if (rawCursor == 65535) -1 else rawCursor
        return SetLocOpOverride(overrideStart, operation, label, cursor, overrideEnd)
    }
}
