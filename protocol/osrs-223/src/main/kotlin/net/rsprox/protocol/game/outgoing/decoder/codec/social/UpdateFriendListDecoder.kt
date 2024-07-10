package net.rsprox.protocol.game.outgoing.decoder.codec.social

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.message.codec.MessageDecoder
import net.rsprot.protocol.metadata.Consistent
import net.rsprot.protocol.tools.MessageDecodingTools
import net.rsprox.protocol.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.game.outgoing.model.social.UpdateFriendList

@Consistent
public class UpdateFriendListDecoder : MessageDecoder<UpdateFriendList> {
    override val prot: ClientProt = GameServerProt.UPDATE_FRIENDLIST

    override fun decode(
        buffer: JagByteBuf,
        tools: MessageDecodingTools,
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
