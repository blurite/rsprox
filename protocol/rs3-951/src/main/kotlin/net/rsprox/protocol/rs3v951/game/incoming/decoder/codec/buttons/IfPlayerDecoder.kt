package net.rsprox.protocol.rs3v951.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfPlayer
import net.rsprox.protocol.rs3v951.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class IfPlayerDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfPlayer> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfPlayer {
        val operation = buffer.g1Alt1()
        val combinedId = buffer.g4Alt1()
        val crmType = buffer.g1Alt1()
        val crmName = buffer.readNativeString()
        val sub = buffer.g2Alt1()
        return IfPlayer(
            crmName,
            combinedId,
            operation,
            sub,
            crmType,
        )
    }
}
