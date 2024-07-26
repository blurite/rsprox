package net.rsprox.transcriber.base

import net.rsprox.shared.SessionMonitor
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.state.StateTracker

public class MonitoredMessageConsumerContainer(
    root: MessageConsumerContainer,
    private val stateTracker: StateTracker,
    private val monitor: SessionMonitor<*>,
) : MessageConsumerContainer by root {
    override fun publish(message: String) {
        super.publish(message)
        monitor.onTranscribe(stateTracker.cycle, message)
    }
}
