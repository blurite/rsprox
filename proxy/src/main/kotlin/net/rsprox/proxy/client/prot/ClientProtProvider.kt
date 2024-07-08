package net.rsprox.proxy.client.prot

public fun interface ClientProtProvider<T> {
    public operator fun get(opcode: Int): T
}
