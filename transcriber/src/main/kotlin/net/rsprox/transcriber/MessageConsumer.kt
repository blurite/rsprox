package net.rsprox.transcriber

import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty

public interface MessageConsumer {
    public fun consume(
        formatter: PropertyTreeFormatter,
        cycle: Int,
        property: RootProperty,
    )

    public fun close()

    public companion object {
        public val STDOUT_CONSUMER: MessageConsumer =
            object : MessageConsumer {
                var lastCycle = -1

                override fun consume(
                    formatter: PropertyTreeFormatter,
                    cycle: Int,
                    property: RootProperty,
                ) {
                    if (cycle != lastCycle) {
                        lastCycle = cycle
                        println("[$cycle]")
                    }
                    val result = formatter.format(property)
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
