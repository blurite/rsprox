package net.rsprox.proxy.client

public fun interface ProtProvider<T> {
    public operator fun get(opcode: Int): T
}
