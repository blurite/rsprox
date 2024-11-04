package net.rsprox.protocol.v223.game.incoming.decoder.codec.misc.user

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.incoming.model.misc.user.Teleport
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

internal class TeleportDecoder : ProxyMessageDecoder<Teleport> {
    override val prot: ClientProt = GameClientProt.TELEPORT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): Teleport {
        val x = buffer.g2Alt1()
        val z = buffer.g2Alt3()
        val level = buffer.g1Alt2()
        val oculusSyncValue = buffer.g4Alt1()
        return Teleport(
            oculusSyncValue,
            x,
            z,
            level,
        )
    }
}
