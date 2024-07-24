package net.rsprox.transcriber

import net.rsprox.cache.api.CacheProvider

public fun interface TranscriberProvider {
    public fun provide(
        container: MessageConsumerContainer,
        cacheProvider: CacheProvider,
    ): TranscriberRunner
}
