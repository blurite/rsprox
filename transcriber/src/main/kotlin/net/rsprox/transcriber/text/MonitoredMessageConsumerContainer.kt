package net.rsprox.transcriber.text

import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.transcriber.BaseMessageConsumerContainer
import net.rsprox.transcriber.MessageConsumerContainer

public class MonitoredMessageConsumerContainer(
    private val root: BaseMessageConsumerContainer,
    private val monitor: SessionMonitor<*>,
) : MessageConsumerContainer {
    override fun publish(
        formatter: PropertyTreeFormatter,
        cycle: Int,
        property: RootProperty,
    ) {
        root.publish(formatter, cycle, property)
        monitor.onTranscribe(cycle, property)
    }

    override fun close() {
        root.close()
    }
}
