package net.rsprox.protocol.rs3v951beta.game.outgoing.decoder.prot

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot

internal enum class GameZoneProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    OBJ_REVEAL(0, 8),
    SOUND_AREA_V2(3, 11),
    OBJ_ADD_V2(4, 6),
    OBJ_REVEAL_V3(9, Prot.VAR_BYTE),
}
