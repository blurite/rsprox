package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.social.UpdateFriendlist
import net.rsprox.protocol.rs3v950.buffer.readNativeString
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateFriendlistDecoder : ProxyMessageDecoder<UpdateFriendlist> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDLIST

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateFriendlist {
        val friends = mutableListOf<UpdateFriendlist.Friend>()
        while (buffer.isReadable) {
            val rename = buffer.g1()
            val name = buffer.readNativeString()
            val previousName = buffer.readNativeString()
            val world = buffer.g2()
            val rank = buffer.g1()
            val flags = buffer.g1()
            val worldName = if (world != 0) buffer.readNativeString() else null
            val platform = if (world != 0) buffer.g1() else null
            val worldMetadata = if (world != 0) buffer.g4() else null
            val note = buffer.readNativeString()
            friends +=
                UpdateFriendlist.Friend(
                    rename,
                    name,
                    previousName,
                    world,
                    rank,
                    flags,
                    worldName,
                    platform,
                    worldMetadata,
                    note,
                )
        }
        return UpdateFriendlist(friends)
    }
}
