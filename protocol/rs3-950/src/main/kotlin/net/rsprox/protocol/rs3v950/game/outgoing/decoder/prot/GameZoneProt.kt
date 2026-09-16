package net.rsprox.protocol.rs3v950.game.outgoing.decoder.prot

import net.rsprot.protocol.ClientProt

/** Zone selector IDs are a separate namespace from top-level server opcodes. */
internal enum class GameZoneProt(
    override val opcode: Int,
    override val size: Int,
) : ClientProt {
    SOUND_AREA_V2(1, 11),
    OBJ_REVEAL_V2(9, 8),
}
