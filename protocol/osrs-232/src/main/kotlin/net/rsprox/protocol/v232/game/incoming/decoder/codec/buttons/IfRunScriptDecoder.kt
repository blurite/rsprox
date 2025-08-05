package net.rsprox.protocol.v232.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedIdAlt3
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.IfRunScript
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt

public class IfRunScriptDecoder : ProxyMessageDecoder<IfRunScript> {
    override val prot: ClientProt = GameClientProt.IF_RUNSCRIPT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfRunScript {
        val sub = buffer.g2()
        val combinedId = buffer.gCombinedIdAlt3()
        val obj = buffer.g2Alt3()
        val script = buffer.g4Alt1()
        val bytes = buffer.buffer.toByteArray()
        return IfRunScript(
            combinedId,
            sub,
            obj,
            script,
            bytes,
        )
    }
}
