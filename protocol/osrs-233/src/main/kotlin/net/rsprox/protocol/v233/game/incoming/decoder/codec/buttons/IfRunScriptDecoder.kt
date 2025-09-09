package net.rsprox.protocol.v233.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.IfRunScript
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class IfRunScriptDecoder : ProxyMessageDecoder<IfRunScript> {
    override val prot: ClientProt = GameClientProt.IF_RUNSCRIPT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfRunScript {
        // Function is method(int combinedId, int sub, int obj, int script, Object[] args)
        // The order of argument does not seem to change (based on two revisions)
        val script = buffer.g4Alt1()
        val combinedId = buffer.gCombinedId()
        val obj = buffer.g2Alt1()
        val sub = buffer.g2Alt2()
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
