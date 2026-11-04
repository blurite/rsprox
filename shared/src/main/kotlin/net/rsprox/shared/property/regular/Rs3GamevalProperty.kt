package net.rsprox.shared.property.regular

import net.rsprox.cache.api.rs3.Rs3Gameval
import net.rsprox.shared.property.ChildProperty

public class Rs3GamevalProperty(
    override val propertyName: String,
    public val gameval: Rs3Gameval,
    override val value: Int,
) : ChildProperty<Int> {
    override val children: MutableList<ChildProperty<*>> = mutableListOf()
    override val type: Class<Int> = Int::class.java
}
