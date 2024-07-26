package net.rsprox.transcriber

public interface MessageConsumerContainer {
    public fun publish(message: List<String>)

    public fun publish(message: String) {
        publish(listOf(message))
    }

    public fun close()
}
