package net.rsprox.protocol.v225.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.misc.player.SetMapFlag
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v225.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class SetMapFlagDecoder : ProxyMessageDecoder<SetMapFlag> {
    override val prot: ClientProt = GameServerProt.SET_MAP_FLAG

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): SetMapFlag {
        val xInBuildArea = buffer.g1()
        val zInBuildArea = buffer.g1()
        return SetMapFlag(
            xInBuildArea,
            zInBuildArea,
        )
    }
}
