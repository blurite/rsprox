package net.rsprox.transcriber.properties

public class PropertyBuilder {
    private val properties: MutableList<Property> = mutableListOf()

    public fun property(
        name: String,
        value: Any,
    ) {
        this.properties += Property(name, value)
    }

    public inline fun <T> filteredProperty(
        name: String,
        value: T,
        filter: (T) -> Boolean,
    ) {
        if (!filter(value)) {
            return
        }
        if (value == null) {
            property(name, "null")
        } else {
            property(name, value)
        }
    }

    public fun build(): List<Property> {
        return properties
    }
}
