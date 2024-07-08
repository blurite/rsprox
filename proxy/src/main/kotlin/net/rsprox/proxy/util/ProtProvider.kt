package net.rsprox.proxy.util

public fun interface ProtProvider<out T> {
    public operator fun get(opcode: Int): T
}
