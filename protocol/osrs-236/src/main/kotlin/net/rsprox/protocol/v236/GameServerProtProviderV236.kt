package net.rsprox.protocol.v236

import net.rsprox.protocol.ProtProvider
import net.rsprox.protocol.v236.game.outgoing.decoder.prot.GameServerProt
import kotlin.enums.EnumEntries

public data object GameServerProtProviderV236 : ProtProvider<GameServerProt> {
    override fun get(opcode: Int): GameServerProt {
        return GameServerProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown game server prot: $opcode")
    }

    override fun allProts(): EnumEntries<GameServerProt> {
        return GameServerProt.entries
    }
}
