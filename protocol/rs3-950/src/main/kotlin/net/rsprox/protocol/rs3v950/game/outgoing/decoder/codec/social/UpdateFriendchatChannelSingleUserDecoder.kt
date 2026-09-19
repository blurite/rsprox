package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendchatChannelSingleUser
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateFriendchatChannelSingleUserDecoder : ProxyMessageDecoder<UpdateFriendchatChannelSingleUser> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_SINGLEUSER

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateFriendchatChannelSingleUser {
        val name = buffer.readNativeString()
        val aliasFlag = buffer.g1()
        val alias = if (aliasFlag == 1) buffer.readNativeString() else null
        val world = buffer.g2()
        val rank = buffer.g1s()
        val worldName = if (rank == -128) null else buffer.readNativeString()
        return UpdateFriendchatChannelSingleUser(name, aliasFlag, alias, world, rank, worldName)
    }
}
