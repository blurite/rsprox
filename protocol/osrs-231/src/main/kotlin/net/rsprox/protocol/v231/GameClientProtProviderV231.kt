package net.rsprox.protocol.v231

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v231.game.incoming.decoder.prot.GameClientProt
import kotlin.enums.EnumEntries

public data object GameClientProtProviderV231 : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }

    override fun allProts(): EnumEntries<GameClientProt> {
        return GameClientProt.entries
    }
}
