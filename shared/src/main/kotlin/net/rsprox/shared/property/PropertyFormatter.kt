package net.rsprox.shared.property

public fun interface PropertyFormatter<in T : Property> {
    public fun format(property: T): String
}
