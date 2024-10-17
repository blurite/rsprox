package net.rsprox.protocol

import net.rsprox.protocol.game.incoming.decoder.prot.GameClientProt

public data object GameClientProtProvider : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
