package net.rsprox.shared.property

public fun interface PropertyTreeFormatter {
    public fun format(property: RootProperty): List<String>
}
