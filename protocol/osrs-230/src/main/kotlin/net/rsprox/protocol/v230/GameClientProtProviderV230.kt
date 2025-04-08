package net.rsprox.protocol.v230

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v230.game.incoming.decoder.prot.GameClientProt
import kotlin.enums.EnumEntries

public data object GameClientProtProviderV230 : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }

    override fun allProts(): EnumEntries<GameClientProt> {
        return GameClientProt.entries
    }
}
