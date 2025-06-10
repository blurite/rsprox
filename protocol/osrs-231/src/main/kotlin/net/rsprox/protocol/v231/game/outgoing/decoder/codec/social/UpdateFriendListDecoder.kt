package net.rsprox.protocol.v231.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.social.UpdateFriendList
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v231.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class UpdateFriendListDecoder : ProxyMessageDecoder<UpdateFriendList> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDLIST

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateFriendList {
        return UpdateFriendList(
            buildList {
                while (buffer.isReadable) {
                    val added = buffer.g1() == 1
                    val name = buffer.gjstr()
                    val previousName = buffer.gjstr()
                    val worldId = buffer.g2()
                    val rank = buffer.g1()
                    val properties = buffer.g1()
                    if (worldId > 0) {
                        val worldName = buffer.gjstr()
                        val platform = buffer.g1()
                        val worldFlags = buffer.g4()
                        val notes = buffer.gjstr()
                        add(
                            UpdateFriendList.OnlineFriend(
                                added,
                                name,
                                previousName,
                                worldId,
                                rank,
                                properties,
                                notes,
                                worldName,
                                platform,
                                worldFlags,
                            ),
                        )
                        continue
                    }
                    val notes = buffer.gjstr()
                    add(
                        UpdateFriendList.OfflineFriend(
                            added,
                            name,
                            previousName,
                            rank,
                            properties,
                            notes,
                        ),
                    )
                }
            },
        )
    }
}
