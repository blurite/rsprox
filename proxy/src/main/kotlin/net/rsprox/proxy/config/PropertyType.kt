package net.rsprox.proxy.config

public sealed interface PropertyType<T>

public data object IntProperty : PropertyType<Int>

public data object StringProperty : PropertyType<String>

public data object BooleanProperty : PropertyType<Boolean>
