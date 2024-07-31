package net.rsprox.transcriber

import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty

public interface MessageConsumerContainer {
    public fun publish(
        formatter: PropertyTreeFormatter,
        cycle: Int,
        property: RootProperty<*>,
    )

    public fun close()
}
