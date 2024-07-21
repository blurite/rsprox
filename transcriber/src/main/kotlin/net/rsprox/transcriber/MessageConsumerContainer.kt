package net.rsprox.transcriber

import com.github.michaelbull.logging.InlineLogger

public class MessageConsumerContainer(
    private val consumers: List<MessageConsumer>,
) {
    public fun publish(message: List<String>) {
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

    public fun publish(message: String) {
        publish(listOf(message))
    }

    public fun close() {
        for (consumer in consumers) {
            consumer.close()
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
