package net.rsprox.shared.property

public interface ChildProperty<T> : Property {
    public val type: Class<T>
    public val propertyName: String
    public val value: T
    override val children: MutableList<ChildProperty<*>>
}
