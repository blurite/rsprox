package net.rsprox.protocol.rs3v951beta.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.IfPlayer
import net.rsprox.protocol.rs3v951beta.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class IfPlayerDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<IfPlayer> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfPlayer {
        val sub = buffer.g2Alt3()
        val crmName = buffer.readNativeString()
        val crmType = buffer.g1Alt3()
        val combinedId = buffer.g4Alt2()
        val operation = buffer.g1Alt1()
        return IfPlayer(
            crmName,
            combinedId,
            operation,
            sub,
            crmType,
        )
    }
}
