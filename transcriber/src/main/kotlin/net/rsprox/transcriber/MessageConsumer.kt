package net.rsprox.transcriber

import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.RootProperty

public interface MessageConsumer {
    public fun consume(
        cycle: Int,
        property: RootProperty<*>,
    )

    public fun close()

    public companion object {
        public val STDOUT_CONSUMER: MessageConsumer =
            object : MessageConsumer {
                val propertyTreeFormatter = OmitFilteredPropertyTreeFormatter()
                var lastCycle = -1

                override fun consume(
                    cycle: Int,
                    property: RootProperty<*>,
                ) {
                    if (cycle != lastCycle) {
                        lastCycle = cycle
                        println("[$cycle]")
                    }
                    val result = propertyTreeFormatter.format(property)
                    for (line in result) {
                        // Add four space indentation due to the cycle header
                        print("    ")
                        println(line)
                    }
                }

                override fun close() {
                }
            }
    }
}
