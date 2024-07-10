package net.rsprox.protocol.game.outgoing.decoder.codec.clan

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.clan.VarClan

@Consistent
public class VarClanDecoder : MessageDecoder<VarClan> {
    override val prot: ClientProt = GameServerProt.VARCLAN

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): VarClan {
        val id = buffer.g1()
        val data = ByteArray(buffer.readableBytes())
        buffer.gdata(data)
        return VarClan(id, VarClan.UnknownVarClanData(data))
    }
}
