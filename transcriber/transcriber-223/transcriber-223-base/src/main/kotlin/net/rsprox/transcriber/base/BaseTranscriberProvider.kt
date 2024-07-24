package net.rsprox.transcriber.base

import net.rsprox.cache.api.CacheProvider
import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.TranscriberPlugin
import net.rsprox.transcriber.TranscriberProvider
import net.rsprox.transcriber.TranscriberRunner

public class BaseTranscriberProvider : TranscriberProvider {
    override fun provide(
        container: MessageConsumerContainer,
        cacheProvider: CacheProvider,
    ): TranscriberRunner {
        return TranscriberPlugin(
            BaseTranscriber(
                container,
                cacheProvider,
            ),
        )
    }
}
