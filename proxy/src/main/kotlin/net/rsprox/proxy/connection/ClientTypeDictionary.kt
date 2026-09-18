package net.rsprox.proxy.connection

import java.util.concurrent.ConcurrentHashMap

public data object ClientTypeDictionary {
    private val dictionary = ConcurrentHashMap<Int, String>()

    public operator fun get(port: Int): String {
        return dictionary[port]
            ?: throw IllegalArgumentException("Port $port has not been registered.")
    }

    public operator fun set(
        port: Int,
        name: String,
    ) {
        val old = this.dictionary.putIfAbsent(port, name)
        if (old != null) {
            throw IllegalArgumentException("Port $port registered more than once ($name/$old)")
        }
    }

    public fun remove(port: Int) {
        dictionary.remove(port)
    }
}
