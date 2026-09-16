package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.LogoutTransfer
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.rs3v950.buffer.readNativeString

internal class LogoutTransferDecoder : ProxyMessageDecoder<LogoutTransfer> {
    override val prot: ClientProt = GameServerProt.LOGOUT_TRANSFER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): LogoutTransfer {
        val world = buffer.g2()
        val host = buffer.readNativeString()
        val port = buffer.g2()
        val alternatePort = buffer.g2()
        val transferFlag = buffer.g1()
        return LogoutTransfer(world, host, port, alternatePort, transferFlag)
    }
}
