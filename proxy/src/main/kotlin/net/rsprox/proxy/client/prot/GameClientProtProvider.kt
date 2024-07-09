package net.rsprox.proxy.client.prot

import net.rsprox.protocol.ProtProvider

public data object GameClientProtProvider : ProtProvider<GameClientProt> {
    override fun get(opcode: Int): GameClientProt {
        return GameClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game client prot: $opcode")
    }
}
