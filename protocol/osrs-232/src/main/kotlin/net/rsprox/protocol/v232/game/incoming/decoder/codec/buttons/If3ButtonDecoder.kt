package net.rsprox.protocol.v232.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt

@Consistent
public class If3ButtonDecoder(
    override val prot: GameClientProt,
    private val op: Int,
) : ProxyMessageDecoder<If3Button> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): If3Button {
        val combinedId = buffer.gCombinedId()
        val sub = buffer.g2()
        val obj = buffer.g2()
        return If3Button(
            combinedId,
            sub,
            obj,
            op,
        )
    }
}
