package net.rsprox.protocol

import kotlin.enums.EnumEntries

public interface ProtProvider<out T> {
    public operator fun get(opcode: Int): T

    public fun allProts(): EnumEntries<*>
}
