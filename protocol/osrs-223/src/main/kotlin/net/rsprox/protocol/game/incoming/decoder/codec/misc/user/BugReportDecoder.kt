package net.rsprox.protocol.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.misc.user.BugReport

@Consistent
public class BugReportDecoder : MessageDecoder<BugReport> {
    override val prot: ClientProt = GameClientProt.BUG_REPORT

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): BugReport {
        val description = buffer.gjstr()
        val type = buffer.g1Alt3()
        val instructions = buffer.gjstr()
        check(description.length <= 500) {
            "Bug report description length cannot exceed 500 characters."
        }
        check(instructions.length <= 500) {
            "Bug report instructions length cannot exceed 500 characters."
        }
        return BugReport(
            type,
            description,
            instructions,
        )
    }
}
