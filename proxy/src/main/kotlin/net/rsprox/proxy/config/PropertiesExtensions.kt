package net.rsprox.proxy.config

import java.util.Properties

internal fun <T> Properties.setValue(
    property: ProxyProperty<T>,
    value: T,
): Properties {
    put(property.name, value.toString())
    return this
}

internal fun <T> Properties.getValue(property: ProxyProperty<T>): T {
    val stringValue = getProperty(property.name)
    val result =
        when (property.type) {
            IntProperty -> stringValue.toInt()
            StringProperty -> stringValue
        }
    @Suppress("UNCHECKED_CAST")
    return result as T
}

internal fun <T> Properties.getValueOrNull(property: ProxyProperty<T>): T? {
    val stringValue = getProperty(property.name) ?: return null
    val result =
        when (property.type) {
            IntProperty -> stringValue.toInt()
            StringProperty -> stringValue
        }
    @Suppress("UNCHECKED_CAST")
    return result as? T
}
