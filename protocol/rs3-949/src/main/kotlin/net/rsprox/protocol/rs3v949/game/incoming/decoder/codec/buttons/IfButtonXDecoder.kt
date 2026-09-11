package net.rsprox.protocol.rs3v949.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.buttons.If3Button
import net.rsprox.protocol.rs3v949.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.session.Session

internal class IfButtonXDecoder(
    override val prot: GameClientProt,
    private val op: Int,
) : ProxyMessageDecoder<If3Button> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): If3Button {
        val combinedId = buffer.gCombinedId()
        val obj = buffer.g3()
        val slot = buffer.g2()
        return If3Button(
            combinedId,
            obj,
            slot,
            op,
        )
    }
}
