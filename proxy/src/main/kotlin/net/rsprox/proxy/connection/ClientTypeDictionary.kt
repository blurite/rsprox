package net.rsprox.proxy.connection

public data object ClientTypeDictionary {
    private val dictionary: MutableMap<Int, String> = mutableMapOf()

    public operator fun get(port: Int): String {
        return dictionary[port]
            ?: throw IllegalArgumentException("Port $port has not been registered.")
    }

    public operator fun set(
        port: Int,
        name: String,
    ) {
        val old = this.dictionary.put(port, name)
        if (old != null) {
            throw IllegalArgumentException("Port $port registered more than once ($name/$old)")
        }
    }
}
