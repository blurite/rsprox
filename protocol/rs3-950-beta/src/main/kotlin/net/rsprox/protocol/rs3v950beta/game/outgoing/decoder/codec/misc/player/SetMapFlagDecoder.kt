package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.misc.player.SetMapFlag
import net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class SetMapFlagDecoder : ProxyMessageDecoder<SetMapFlag> {
    override val prot: ClientProt = GameServerProt.SET_MAP_FLAG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetMapFlag {
        val id = buffer.g4Alt2()
        val type = buffer.g1Alt3().toByte().toInt()
        val sourceZ = buffer.g1Alt3()
        val targetZ = buffer.g1Alt1()
        val sourceX = buffer.g1Alt1()
        val targetX = buffer.g1()
        val flags = buffer.g1Alt2()
        return SetMapFlag(
            targetX,
            type,
            flags,
            sourceZ,
            id,
            sourceX,
            targetZ,
        )
    }
}
