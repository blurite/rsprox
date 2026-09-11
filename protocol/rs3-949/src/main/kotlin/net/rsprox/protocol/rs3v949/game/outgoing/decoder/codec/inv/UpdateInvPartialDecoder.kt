package net.rsprox.protocol.rs3v949.game.outgoing.decoder.codec.inv

import net.rsprot.buffer.JagByteBuf
import net.rsprot.protocol.ClientProt
import net.rsprox.protocol.ProxyMessageDecoder
import net.rsprox.protocol.rs3v949.game.outgoing.decoder.prot.GameServerProt
import net.rsprox.protocol.rs3.game.outgoing.model.inv.util.InvVar
import net.rsprox.protocol.rs3.game.outgoing.model.inv.UpdateInvPartial
import net.rsprox.protocol.session.Session

internal class UpdateInvPartialDecoder : ProxyMessageDecoder<UpdateInvPartial> {
    override val prot: ClientProt = GameServerProt.UPDATE_INV_PARTIAL

    override fun decode(
        buffer: JagByteBuf,
        session: Session,
    ): UpdateInvPartial {
        val inventoryId = buffer.g2()
        val flags = buffer.g1()
        val hasVars = (flags and 0x2) != 0
        val objs =
            buildList {
                while (buffer.isReadable) {
                    val slot = buffer.gSmart1or2()
                    if (buffer.readableBytes() < 3) break
                    val rawObjId = buffer.g3()
                    if (rawObjId == 0) {
                        add(UpdateInvPartial.IndexedObj(slot, -1, 0, emptyList()))
                        continue
                    }
                    var amount = buffer.g1()
                    if (amount == 0xFF) {
                        amount = buffer.g4()
                    }
                    val vars =
                        if (hasVars && buffer.isReadable) {
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
                    add(UpdateInvPartial.IndexedObj(slot, rawObjId - 1, amount, vars))
                }
            }
        return UpdateInvPartial(
            inventoryId,
            flags,
            objs,
        )
    }
}
