package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.misc.client

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.client.DoCheat
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.rs3v950.buffer.readNativeString

internal class DoCheatDecoder : ProxyMessageDecoder<DoCheat> {
    override val prot: ClientProt = GameServerProt.DO_CHEAT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): DoCheat {
        val command = buffer.readNativeString()
        return DoCheat(command)
    }
}
