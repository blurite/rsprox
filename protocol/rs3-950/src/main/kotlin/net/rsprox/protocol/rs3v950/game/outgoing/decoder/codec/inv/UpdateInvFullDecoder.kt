package net.rsprox.protocol.rs3v950.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvFull
import net.rsprox.protocol.rs3.game.outgoing.model.inv.util.InvVar
import net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.session.Session

internal class UpdateInvFullDecoder : ProxyMessageDecoder<UpdateInvFull> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_FULL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateInvFull {
        val inventoryId = buffer.g2()
        val flags = buffer.g1()
        val hasVars = (flags and 0x2) != 0
        val slotCount = buffer.g2()
        val objs =
            buildList {
                for (slot in 0 until slotCount) {
                    val rawObjId = buffer.g3()
                    var amount = buffer.g1()
                    if (amount == 0xFF) {
                        amount = buffer.g4()
                    }
                    val vars =
                        if (hasVars) {
                            val varCount = buffer.g1()
                            buildList {
                                for (v in 0 until varCount) {
                                    val varId = buffer.g2()
                                    val varVal = buffer.g4()
                                    add(InvVar(varId, varVal))
                                }
                            }
                        } else {
                            emptyList()
                        }
                    add(UpdateInvFull.Obj(rawObjId - 1, amount, vars))
                }
            }
        return UpdateInvFull(
            inventoryId,
            flags,
            objs,
        )
    }
}
