package net.rsprox.protocol.v225

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v225.game.incoming.decoder.prot.GameClientProt

public data object GameClientProtProviderV225 : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
