package net.rsprox.protocol.rs3v950.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.incoming.model.misc.user.ClientCheat
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.session.Session

internal class ClientCheatDecoder(
    override val prot: ClientProt,
) : ProxyMessageDecoder<ClientCheat> {
    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): ClientCheat {
        val flagA = buffer.g1()
        val flagB = buffer.g1()
        val command = buffer.readNativeString()
        return ClientCheat(
            flagA,
            flagB,
            command,
        )
    }
}
