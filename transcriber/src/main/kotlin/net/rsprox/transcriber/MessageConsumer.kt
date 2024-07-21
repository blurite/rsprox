package net.rsprox.transcriber

public interface MessageConsumer {
    public fun consume(message: List<String>)

    public fun close()
}
