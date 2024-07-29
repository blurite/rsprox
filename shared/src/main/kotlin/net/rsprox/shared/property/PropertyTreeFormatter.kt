package net.rsprox.shared.property

public fun interface PropertyTreeFormatter {
    public fun format(
        cycle: Int,
        property: RootProperty<*>,
    ): StringPropertyTree
}
