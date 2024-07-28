package net.rsprox.shared.property

public interface RootProperty<T> : Property {
    public val prot: T
    override val children: MutableList<ChildProperty<*>>
}
