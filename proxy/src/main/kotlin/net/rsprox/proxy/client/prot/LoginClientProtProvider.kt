package net.rsprox.proxy.client.prot

import net.rsprox.protocol.ProtProvider
import kotlin.enums.EnumEntries

public data object LoginClientProtProvider : ProtProvider<LoginClientProt> {
    override fun get(opcode: Int): LoginClientProt {
        return LoginClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown login client prot: $opcode")
    }

    override fun allProts(): EnumEntries<*> {
        return LoginClientProt.entries
    }
}
