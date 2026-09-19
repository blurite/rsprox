package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendchatChannelFull
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateFriendchatChannelFullDecoder : ProxyMessageDecoder<UpdateFriendchatChannelFull> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDCHAT_CHANNEL_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateFriendchatChannelFull {
        if (!buffer.isReadable) return UpdateFriendchatChannelFull(null)
        val owner = buffer.readNativeString()
        val aliasFlag = buffer.g1()
        val alias = if (aliasFlag == 1) buffer.readNativeString() else null
        val name = buffer.readNativeString()
        val minimumKickRank = buffer.g1s()
        val memberCount = buffer.gSmart1or2() - 1
        require(memberCount <= buffer.readableBytes() / 6) { "Truncated friend-chat member list" }
        val members =
            if (memberCount == -1) {
                null // Preserve the existing members, unlike an empty replacement list.
            } else {
                List(memberCount) {
                    val memberName = buffer.readNativeString()
                    val memberAliasFlag = buffer.g1()
                    val memberAlias = if (memberAliasFlag == 1) buffer.readNativeString() else null
                    val world = buffer.g2()
                    val rank = buffer.g1s()
                    val worldName = buffer.readNativeString()
                    UpdateFriendchatChannelFull.Member(memberName, memberAliasFlag, memberAlias, world, rank, worldName)
                }
            }
        return UpdateFriendchatChannelFull(
            UpdateFriendchatChannelFull.Channel(owner, aliasFlag, alias, name, minimumKickRank, memberCount, members),
        )
    }
}
