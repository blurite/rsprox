package net.rsprox.proxy.configuration

internal sealed interface PropertyType<T>

internal data object IntProperty : PropertyType<Int>

internal data object StringProperty : PropertyType<String>
