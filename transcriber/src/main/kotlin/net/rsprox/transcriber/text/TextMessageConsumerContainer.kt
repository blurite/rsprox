package net.rsprox.transcriber.text

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.shared.property.PropertyTreeFormatter
import net.rsprox.shared.property.RootProperty
import net.rsprox.transcriber.MessageConsumer
import net.rsprox.transcriber.MessageConsumerContainer

public open class TextMessageConsumerContainer(
    private val consumers: List<MessageConsumer>,
) : MessageConsumerContainer {
    override fun publish(
        formatter: PropertyTreeFormatter,
        cycle: Int,
        property: RootProperty,
    ) {
        for (consumer in consumers) {
            try {
                consumer.consume(formatter, cycle, property)
            } catch (e: Exception) {
                logger.error(e) {
                    "Unable to publish message: $property"
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
