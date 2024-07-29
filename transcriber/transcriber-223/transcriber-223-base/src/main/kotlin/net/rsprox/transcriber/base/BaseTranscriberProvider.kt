package net.rsprox.transcriber.base

import net.rsprox.cache.api.CacheProvider
import net.rsprox.shared.SessionMonitor
import net.rsprox.transcriber.BaseMessageConsumerContainer
import net.rsprox.transcriber.TranscriberPlugin
import net.rsprox.transcriber.TranscriberProvider
import net.rsprox.transcriber.TranscriberRunner
import net.rsprox.transcriber.state.StateTracker

public class BaseTranscriberProvider : TranscriberProvider {
    override fun provide(
        container: BaseMessageConsumerContainer,
        cacheProvider: CacheProvider,
        monitor: SessionMonitor<*>,
    ): TranscriberRunner {
        val stateTracker = StateTracker()
        return TranscriberPlugin(
            BaseTranscriber(
                cacheProvider,
                monitor,
                stateTracker,
                container,
            ),
        )
    }
}
