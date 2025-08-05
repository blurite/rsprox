package net.rsprox.protocol.v232.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.game.incoming.model.misc.user.Teleport
import net.rsprox.protocol.v232.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.ProxyMessageDecoder

public class TeleportDecoder : ProxyMessageDecoder<Teleport> {
    override val prot: ClientProt = GameClientProt.TELEPORT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Teleport {
        val oculusSyncValue = buffer.g4Alt3()
        val z = buffer.g2Alt1()
        val level = buffer.g1Alt1()
        val x = buffer.g2()
        return Teleport(
            oculusSyncValue,
            x,
            z,
            level,
        )
    }
}
