package net.rsprox.proxy.client

public data object LoginClientProtProvider : ProtProvider<LoginClientProt> {
    override fun get(opcode: Int): LoginClientProt {
        return LoginClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown login client prot: $opcode")
    }
}
