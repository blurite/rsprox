package net.rsprox.protocol.v226

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v226.game.incoming.decoder.prot.GameClientProt

public data object GameClientProtProvider : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
