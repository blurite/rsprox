package net.rsprox.shared.property

public interface RootProperty : Property {
    public val prot: String
    override val children: MutableList<ChildProperty<*>>
}
