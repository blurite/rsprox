package net.rsprox.protocol.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.social.FriendListLoaded

@Consistent
public class FriendListLoadedDecoder : MessageDecoder<FriendListLoaded> {
    override val prot: ClientProt = GameServerProt.FRIENDLIST_LOADED

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
    ): FriendListLoaded {
        return FriendListLoaded
    }
}
