package net.rsprox.proxy.util

public fun interface ProtProvider<T> {
    public operator fun get(opcode: Int): T
}
