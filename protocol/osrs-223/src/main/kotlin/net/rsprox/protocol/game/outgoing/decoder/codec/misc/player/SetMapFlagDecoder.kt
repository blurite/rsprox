package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.SetMapFlag

@Consistent
public class SetMapFlagDecoder : MessageDecoder<SetMapFlag> {
    override val prot: ClientProt = GameServerProt.SET_MAP_FLAG

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): SetMapFlag {
        val xInBuildArea = buffer.g1()
        val zInBuildArea = buffer.g1()
        return SetMapFlag(
            xInBuildArea,
            zInBuildArea,
        )
    }
}
