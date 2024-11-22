package net.rsprox.protocol.v227

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v227.game.incoming.decoder.prot.GameClientProt

public data object GameClientProtProviderV227 : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
