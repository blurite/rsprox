package net.rsprox.proxy.client.prot

public data object LoginClientProtProvider : ClientProtProvider<LoginClientProt> {
    override fun get(opcode: Int): LoginClientProt {
        return LoginClientProt.entries.firstOrNull { it.opcode == opcode }
            ?: throw IllegalArgumentException("Unknown login client prot: $opcode")
    }
}
