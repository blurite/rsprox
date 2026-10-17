package net.rsprox.gui.sessions

import net.rsprox.shared.property.Property
import javax.swing.SwingUtilities

/** Prevents network callbacks from retaining an unbounded backlog on the Swing event queue. */
internal class LiveTranscriptUpdates {
    private var pendingCount = 0
    private var pendingWeight = 0
    private var generation = 0L

    internal fun submit(
        property: Property,
        update: () -> Unit,
    ) {
        val weight = weight(property)
        val submittedGeneration =
            synchronized(this) {
                if (pendingCount >= MAX_PENDING_UPDATES || pendingWeight + weight > MAX_PENDING_WEIGHT) {
                    return
                }
                pendingCount++
                pendingWeight += weight
                generation
            }
        SwingUtilities.invokeLater {
            try {
                if (synchronized(this) { generation == submittedGeneration }) update()
            } finally {
                synchronized(this) {
                    pendingCount--
                    pendingWeight -= weight
                }
            }
        }
    }

    @Synchronized
    internal fun discardPending() {
        generation++
    }

    private fun weight(property: Property): Int {
        var count = 1
        val stack = ArrayDeque<Iterator<Property>>()
        stack.addLast(property.children.iterator())
        while (stack.isNotEmpty()) {
            val children = stack.last()
            if (!children.hasNext()) {
                stack.removeLast()
                continue
            }
            val child = children.next()
            if (++count > MAX_PENDING_WEIGHT || stack.size > MAX_DEPTH) return MAX_PENDING_WEIGHT + 1
            if (child.children.isNotEmpty()) stack.addLast(child.children.iterator())
        }
        return count
    }

    private companion object {
        private const val MAX_PENDING_UPDATES = 128

        // Count all properties, not just visible rows, so collapsed/large packets cannot hide a backlog.
        private const val MAX_PENDING_WEIGHT = 4_998
        private const val MAX_DEPTH = 64
    }
}
