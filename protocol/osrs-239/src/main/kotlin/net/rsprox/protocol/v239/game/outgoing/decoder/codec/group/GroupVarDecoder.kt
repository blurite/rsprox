package net.rsprox.protocol.v239.game.outgoing.decoder.codec.group

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.metadata.Consistent
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.game.outgoing.model.group.GroupVar
import net.rsprox.protocol.game.outgoing.model.group.util.GroupVarUpdate
import net.rsprox.protocol.game.outgoing.model.group.util.GroupVariable
import net.rsprox.protocol.session.Session
import net.rsprox.protocol.v239.game.outgoing.decoder.prot.GameServerProt

@Consistent
internal class GroupVarDecoder : ProxyMessageDecoder<GroupVar> {
    override val prot: ClientProt = GameServerProt.GROUP_VAR

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): GroupVar {
        val updates =
            buildList {
                while (buffer.isReadable) {
                    val index = buffer.g1()
                    val packedGroupVar = buffer.g4()
                    val baseVarType = (packedGroupVar ushr 16) and 0x3
                    val variable =
                        when (baseVarType) {
                            GroupVarUpdate.INT_BASE_VAR_TYPE -> GroupVariable.IntGroupVariable(buffer.g4())
                            GroupVarUpdate.LONG_BASE_VAR_TYPE -> GroupVariable.LongGroupVariable(buffer.g8())
                            GroupVarUpdate.STRING_BASE_VAR_TYPE -> GroupVariable.StringGroupVariable(buffer.gjstr())
                            else -> {
                                val data = ByteArray(buffer.readableBytes())
                                buffer.gdata(data)
                                GroupVariable.UnknownGroupVariable(baseVarType, data)
                            }
                        }
                    add(GroupVarUpdate(index, packedGroupVar, variable))
                }
            }
        return GroupVar(updates)
    }
}