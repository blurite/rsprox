package net.rsprox.protocol.v223.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

@Consistent
public class BugReportDecoder : ProxyMessageDecoder<BugReport> {
    override val prot: ClientProt = GameClientProt.BUG_REPORT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
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
