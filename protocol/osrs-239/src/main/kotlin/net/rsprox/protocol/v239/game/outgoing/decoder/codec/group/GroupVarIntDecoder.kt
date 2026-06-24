package net.rsprox.protocol.v239.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.group.GroupVarInt
import net.rsprox.protocol.game.outgoing.model.group.util.GroupVarUpdate
import net.rsprox.protocol.game.outgoing.model.group.util.GroupVariable
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class GroupVarIntDecoder : ProxyMessageDecoder<GroupVarInt> {
    override val prot: ClientProt = GameServerProt.GROUP_VAR_INT

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): GroupVarInt {
        val index = buffer.g1()
        val packedGroupVar = buffer.g4()
        val value = buffer.g4()
        return GroupVarInt(
            GroupVarUpdate(
                index,
                packedGroupVar,
                GroupVariable.IntGroupVariable(value),
            ),
        )
    }
}
