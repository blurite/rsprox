package net.rsprox.protocol.game.outgoing.decoder.codec.misc.player

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.misc.player.UpdateStatOld

public class UpdateStatOldDecoder : MessageDecoder<UpdateStatOld> {
    override val prot: ClientProt = GameServerProt.UPDATE_STAT_OLD

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): UpdateStatOld {
        val experience = buffer.g4()
        val currentLevel = buffer.g1Alt3()
        val stat = buffer.g1Alt3()
        return UpdateStatOld(
            stat,
            currentLevel,
            experience,
        )
    }
}
