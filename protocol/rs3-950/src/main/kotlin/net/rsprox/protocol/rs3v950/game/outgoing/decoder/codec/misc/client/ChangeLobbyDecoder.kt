package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ChangeLobby
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.rs3v950.buffer.readNativeString

internal class ChangeLobbyDecoder : ProxyMessageDecoder<ChangeLobby> {
    override val prot: ClientProt = GameServerProt.CHANGE_LOBBY

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ChangeLobby {
        val host = buffer.readNativeString()
        val world = buffer.g2()
        val port = buffer.g2()
        val alternatePort = buffer.g2()
        return ChangeLobby(host, world, port, alternatePort)
    }
}
