package net.rsprox.protocol.game.incoming.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt
import net.rsprox.protocol.game.incoming.model.social.FriendListDel

@Consistent
public class FriendListDelDecoder : MessageDecoder<FriendListDel> {
    override val prot: ClientProt = GameClientProt.FRIENDLIST_DEL

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): FriendListDel {
        val name = buffer.gjstr()
        return FriendListDel(name)
    }
}
