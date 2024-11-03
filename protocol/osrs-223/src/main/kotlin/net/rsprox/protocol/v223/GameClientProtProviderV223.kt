package net.rsprox.protocol.v223

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v223.game.incoming.decoder.prot.GameClientProt

public data object GameClientProtProviderV223 : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
