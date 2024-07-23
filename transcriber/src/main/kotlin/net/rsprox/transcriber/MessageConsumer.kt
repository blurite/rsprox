package net.rsprox.transcriber

public interface MessageConsumer {
    public fun consume(message: List<String>)

    public fun close()

    public companion object {
        public val STDOUT_CONSUMER: MessageConsumer =
            object : MessageConsumer {
                override fun consume(message: List<String>) {
                    for (line in message) {
                        println(line)
                    }
                }

                override fun close() {
                }
            }

        public val STDERR_CONSUMER: MessageConsumer =
            object : MessageConsumer {
                override fun consume(message: List<String>) {
                    for (line in message) {
                        System.err.println(line)
                    }
                }

                override fun close() {
                }
            }
    }
}
