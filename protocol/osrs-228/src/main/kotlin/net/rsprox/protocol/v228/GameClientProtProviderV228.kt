package net.rsprox.protocol.v228

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v228.game.incoming.decoder.prot.GameClientProt

public data object GameClientProtProviderV228 : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
