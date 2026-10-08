package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.selection

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.selection.SetLocOpOverride
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetLocOpOverrideDecoder : ProxyMessageDecoder<SetLocOpOverride> {
    override val prot: ClientProt = GameServerProt.SET_LOC_OP_OVERRIDE

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetLocOpOverride {
        val operation = buffer.g1Alt3()
        val rawCursor = buffer.g2Alt3()
        val cursor = if (rawCursor == 65535) -1 else rawCursor
        val label = buffer.gjstr()
        val overrideStart = buffer.g4Alt3()
        val overrideEnd = buffer.g4()
        return SetLocOpOverride(overrideStart, operation, label, cursor, overrideEnd)
    }
}
