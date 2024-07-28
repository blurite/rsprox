package net.rsprox.shared.property

public fun interface PropertyFormatter {
    public fun format(property: RootProperty<*>): List<String>
}
