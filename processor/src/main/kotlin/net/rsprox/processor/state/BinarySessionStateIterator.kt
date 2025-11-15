package net.rsprox.processor.state

import net.rsprot.protocol.message.IncomingMessage

public class BinarySessionStateIterator(
    private val messages: List<IncomingMessage>,
) : Iterable<BinarySessionStateIterator.Entry> {
    override fun iterator(): Iterator<Entry> {
        return EntryIterator(messages)
    }

    public class EntryIterator(private val messages: List<IncomingMessage>) : Iterator<Entry> {
        private var state = BinarySessionState.DEFAULT
        private var cursor = 0

        override fun hasNext(): Boolean {
            return cursor < messages.size
        }

        override fun next(): Entry {
            val message = messages[cursor++]
            state = state.nextState(message)
            return Entry(state, message)
        }
    }

    public class Entry(
        public val state: BinarySessionState,
        public val message: IncomingMessage,
    ) {
        public operator fun component1(): BinarySessionState {
            return state
        }

        public operator fun component2(): IncomingMessage {
            return message
        }
    }
}
