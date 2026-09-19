package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ConsoleFeedback
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ConsoleFeedbackDecoder : ProxyMessageDecoder<ConsoleFeedback> {
    override val prot: ClientProt = GameServerProt.CONSOLE_FEEDBACK

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ConsoleFeedback {
        val unusedText = buffer.readNativeString()
        val prefix = buffer.readNativeString()
        val totalMatches = buffer.g4()
        val resultCount = buffer.g2()
        val results = mutableListOf<String>()
        for (index in 0 until resultCount) {
            val result = buffer.readNativeString()
            results += result // Retain the transmitted terminator as well as nonempty results.
            if (result.isEmpty()) break
        }
        return ConsoleFeedback(unusedText, prefix, totalMatches, resultCount, results)
    }
}
