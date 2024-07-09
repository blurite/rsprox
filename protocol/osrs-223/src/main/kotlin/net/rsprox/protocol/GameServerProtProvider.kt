package net.rsprox.protocol

import net.rsprox.protocol.game.outgoing.prot.GameServerProt

public data object GameServerProtProvider : ProtProvider<GameServerProt> {
    override fun get(opcode: Int): GameServerProt {
        return GameServerProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game server prot: $opcode")
    }
}
