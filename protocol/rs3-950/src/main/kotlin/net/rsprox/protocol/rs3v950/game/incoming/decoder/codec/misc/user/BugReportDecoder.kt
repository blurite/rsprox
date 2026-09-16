package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.BugReport
import net.rsprox.protocol.rs3v950.buffer.readNativeString2
import net.rsprox.protocol.session.Session

internal class BugReportDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<BugReport> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): BugReport {
        val reportCategory = buffer.g1Alt1()
        val details = buffer.readNativeString2()
        val summary = buffer.readNativeString2()
        return BugReport(
            reportCategory,
            details,
            summary,
        )
    }
}
