package net.rsprox.protocol.session

import java.util.IdentityHashMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public class AttributeMap {
    private val attributes: MutableMap<AttributeKey<*>, Any> = IdentityHashMap()

    public operator fun <T> get(key: AttributeKey<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return attributes[key] as? T
    }

    public operator fun <T> set(
        key: AttributeKey<T>,
        value: T?,
    ): T? {
        @Suppress("UNCHECKED_CAST")
        val old =
            if (value == null) {
                attributes.remove(key)
            } else {
                attributes.put(key, value)
            } as? T
        return old
    }
}

public fun <T> attribute(): ReadWriteProperty<Session, T?> = attribute(AttributeKey())

private fun <T> attribute(key: AttributeKey<T>): ReadWriteProperty<Session, T?> {
    return object : ReadWriteProperty<Session, T?> {
        override fun getValue(
            thisRef: Session,
            property: KProperty<*>,
        ): T? = thisRef.attributes[key]

        override fun setValue(
            thisRef: Session,
            property: KProperty<*>,
            value: T?,
        ) {
            thisRef.attributes[key] = value
        }
    }
}
