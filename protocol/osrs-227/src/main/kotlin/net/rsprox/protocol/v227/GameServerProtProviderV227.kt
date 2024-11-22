package net.rsprox.protocol.v227

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v227.game.outgoing.decoder.prot.GameServerProt

public data object GameServerProtProviderV227 : ProtProvider<GameServerProt> {
    override fun get(opcode: Int): GameServerProt {
        return GameServerProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game server prot: $opcode")
    }
}
