package net.rsprox.protocol

public fun interface ProtProvider<out T> {
    public operator fun get(opcode: Int): T
}
