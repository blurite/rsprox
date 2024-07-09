package net.rsprox.proxy.server.prot

import net.rsprox.protocol.ProtProvider

public data object GameServerProtProvider : ProtProvider<GameServerProt> {
    override fun get(opcode: Int): GameServerProt {
        return GameServerProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game server prot: $opcode")
    }
}
