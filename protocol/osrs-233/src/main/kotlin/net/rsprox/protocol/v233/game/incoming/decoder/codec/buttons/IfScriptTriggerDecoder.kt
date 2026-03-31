package net.rsprox.protocol.v233.game.incoming.decoder.codec.buttons

import net.rsprot.buffer.JagByteBuf
import net.rsprot.buffer.extensions.toByteArray
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.util.gCombinedId
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.buttons.IfScriptTrigger
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v233.game.incoming.decoder.prot.GameClientProt

public class IfScriptTriggerDecoder : ProxyMessageDecoder<IfScriptTrigger> {
    override val prot: ClientProt = GameClientProt.IF_SCRIPT_TRIGGER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): IfScriptTrigger {
        // Function is method(int combinedId, int sub, int obj, int crc, Object[] args)
        val crc = buffer.g4Alt1()
        val combinedId = buffer.gCombinedId()
        val obj = buffer.g2Alt1()
        val sub = buffer.g2Alt2()
        val bytes = buffer.buffer.toByteArray()
        return IfScriptTrigger(
            combinedId,
            sub,
            obj,
            crc,
            bytes,
        )
    }
}
