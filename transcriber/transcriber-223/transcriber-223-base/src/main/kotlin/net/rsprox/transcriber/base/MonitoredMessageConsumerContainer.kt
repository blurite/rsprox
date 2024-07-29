package net.rsprox.transcriber.base

import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.RootProperty
import net.rsprox.transcriber.BaseMessageConsumerContainer
import net.rsprox.transcriber.MessageConsumerContainer

public class MonitoredMessageConsumerContainer(
    private val root: BaseMessageConsumerContainer,
    private val monitor: SessionMonitor<*>,
) : MessageConsumerContainer {
    override fun publish(
        cycle: Int,
        property: RootProperty<*>,
    ) {
        root.publish(cycle, property)
        monitor.onTranscribe(cycle, property)
    }

    override fun close() {
        root.close()
    }
}
