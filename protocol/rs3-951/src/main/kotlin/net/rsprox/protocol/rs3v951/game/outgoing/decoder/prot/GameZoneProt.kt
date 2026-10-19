package net.rsprox.protocol.rs3v951.game.outgoing.decoder.prot

import net.rsprot.protocol.ClientProt
import net.rsprot.protocol.Prot

internal enum class GameZoneProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    OBJ_REVEAL(3, 8),
    OBJ_REVEAL_V3(4, Prot.VAR_BYTE),
    SOUND_AREA_V2(8, 11),
    OBJ_ADD_V2(13, 6),
}
