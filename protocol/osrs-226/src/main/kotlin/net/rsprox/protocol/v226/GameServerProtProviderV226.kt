package net.rsprox.protocol.v226

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v226.game.outgoing.decoder.prot.GameServerProt

public data object GameServerProtProviderV226 : ProtProvider<GameServerProt> {
    override fun get(opcode: Int): GameServerProt {
        return GameServerProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game server prot: $opcode")
    }
}
