package net.rsprox.proxy.server.prot

import net.rsprox.protocol.ProtProvider
import kotlin.enums.EnumEntries

public data object LoginServerProtProvider : ProtProvider<LoginServerProt> {
    override fun get(opcode: Int): LoginServerProt {
        return LoginServerProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown login server prot: $opcode")
    }

    override fun allProts(): EnumEntries<*> {
        return LoginServerProt.entries
    }
}
