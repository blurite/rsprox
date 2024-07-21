package net.rsprox.transcriber.base

import net.rsprox.transcriber.MessageConsumerContainer
import net.rsprox.transcriber.TranscriberPlugin
import net.rsprox.transcriber.TranscriberProvider
import net.rsprox.transcriber.TranscriberRunner

public class BaseTranscriberProvider : TranscriberProvider {
    override fun provide(container: MessageConsumerContainer): TranscriberRunner {
        return TranscriberPlugin(BaseTranscriber(container))
    }
}
