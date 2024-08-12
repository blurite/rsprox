package net.rsprox.patch.native.processors

internal fun interface ClientProcessor<out T> {
    fun process(): T
}
