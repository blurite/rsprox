package net.rsprox.transcriber

import com.github.michaelbull.logging.InlineLogger

public class BaseMessageConsumerContainer(
    private val consumers: List<MessageConsumer>,
) : MessageConsumerContainer {
    override fun publish(message: List<String>) {
        for (consumer in consumers) {
            try {
                consumer.consume(message)
            } catch (e: Exception) {
                logger.error(e) {
                    "Unable to publish message: $message"
                }
            }
        }
    }

    override fun close() {
        for (consumer in consumers) {
            consumer.close()
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
