package net.rsprox.transcriber

import net.rsprox.shared.property.RootProperty

public interface MessageConsumerContainer {
    public fun publish(
        cycle: Int,
        property: RootProperty<*>,
    )

    public fun close()
}
