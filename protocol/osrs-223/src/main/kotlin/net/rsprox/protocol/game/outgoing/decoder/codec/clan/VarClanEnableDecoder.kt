package net.rsprox.protocol.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.clan.VarClanEnable

@Consistent
public class VarClanEnableDecoder : MessageDecoder<VarClanEnable> {
    override val prot: ClientProt = GameServerProt.VARCLAN_ENABLE

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): VarClanEnable {
        return VarClanEnable
    }
}
