package net.rsprox.proxy.config

internal sealed interface PropertyType<T>

internal data object IntProperty : PropertyType<Int>

internal data object StringProperty : PropertyType<String>
