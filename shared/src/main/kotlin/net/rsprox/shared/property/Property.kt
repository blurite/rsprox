package net.rsprox.shared.property

public interface Property {
    public val children: MutableList<ChildProperty<*>>

    public fun <R, T : ChildProperty<R>> child(property: T): T {
        this.children += property
        return property
    }
}
