package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.ExecuteClientCheat
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class ExecuteClientCheatDecoder : ProxyMessageDecoder<ExecuteClientCheat> {
    override val prot: ClientProt = GameServerProt.EXECUTE_CLIENT_CHEAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ExecuteClientCheat {
        val command = buffer.g2()
        return ExecuteClientCheat(
            command,
        )
    }
}
