package net.rsprox.protocol.v223.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.If1Button
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

@Consistent
internal class If1ButtonDecoder : ProxyMessageDecoder<If1Button> {
    override val prot: ClientProt = GameClientProt.IF_BUTTON

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): If1Button {
        val combinedId = buffer.gCombinedId()
        return If1Button(combinedId)
    }
}
