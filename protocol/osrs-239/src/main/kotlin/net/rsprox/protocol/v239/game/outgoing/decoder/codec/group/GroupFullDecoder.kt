package net.rsprox.protocol.v239.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.cache.api.CacheProvider
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.group.GroupFull
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class GroupFullDecoder(
    @Suppress("unused")
    private val cache: CacheProvider,
) : ProxyMessageDecoder<GroupFull> {
    override val prot: ClientProt = GameServerProt.GROUP_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): GroupFull {
        val updates =
            buildList {
                while (buffer.isReadable) {
                    val index = buffer.g1()
                    val id = buffer.gSmart1or2null()
                    if (id == -1) {
                        add(GroupFull.GroupDelete(index))
                    } else {
                        val uid = buffer.g8()
                        val data = ByteArray(buffer.readableBytes())
                        buffer.gdata(data)
                        // TODO: Use cache group definitions to decode group and member variables,
                        // then continue walking any following add/change updates in this packet.
                        add(GroupFull.GroupAddChange(index, id, uid, data))
                    }
                }
            }
        return GroupFull(updates)
    }
}
