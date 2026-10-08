package net.rsprox.protocol.rs3v950beta.game.outgoing.decoder.prot

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot

internal enum class GameZoneProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    SOUND_AREA_V2(9, 11),
    OBJ_REVEAL(14, 8),
    OBJ_REVEAL_V3(16, Prot.VAR_BYTE),
    OBJ_ADD_V2(20, 6),
}
