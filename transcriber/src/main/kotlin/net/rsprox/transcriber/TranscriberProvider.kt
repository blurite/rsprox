package net.rsprox.transcriber

public fun interface TranscriberProvider {
    public fun provide(container: MessageConsumerContainer): TranscriberRunner
}
