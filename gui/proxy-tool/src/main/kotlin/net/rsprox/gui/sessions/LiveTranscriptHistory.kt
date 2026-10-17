package net.rsprox.gui.sessions

import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultTreeTableModel
import org.jdesktop.swingx.treetable.TreeTableNode

/** Bounds retained rows, including descendants hidden under collapsed groups. Used only by the live UI. */
internal class LiveTranscriptHistory(
    private val model: DefaultTreeTableModel,
    private val root: AbstractMutableTreeTableNode,
    private val limit: Int = 5_000,
) {
    init {
        require(limit > 0)
    }

    internal var rowCount: Int = 0
        private set

    // Leave room for the stream and tick, and never evict history for a packet that cannot fit.
    internal fun canRetainPacket(node: TreeTableNode): Boolean = count(node) <= limit - 2

    internal fun insert(
        node: AbstractMutableTreeTableNode,
        parent: AbstractMutableTreeTableNode,
    ) {
        model.insertNodeInto(node, parent, parent.childCount)
        rowCount += count(node)
    }

    internal fun trim(retainedStream: AbstractMutableTreeTableNode?) {
        if (rowCount <= limit) return
        val target = (limit.toLong() * 4 / 5).toInt().coerceAtLeast(1)
        while (rowCount > target) {
            val streamIndex =
                (0 until root.childCount).first {
                    val stream = root.getChildAt(it)
                    stream !== retainedStream || stream.childCount > 0
                }
            val stream = root.getChildAt(streamIndex) as AbstractMutableTreeTableNode
            if (stream.childCount == 0) {
                remove(stream)
                continue
            }
            val tick = stream.getChildAt(0) as AbstractMutableTreeTableNode
            // Keep the newest packet if it fits the hard limit but exceeds the lower trim target.
            if (root.childCount == 1 && stream.childCount == 1 && tick.childCount == 1 && rowCount <= limit) break
            if (tick.childCount > 0) {
                // Keep packet subtrees intact rather than leaving orphaned extended-info rows.
                remove(tick.getChildAt(0) as AbstractMutableTreeTableNode)
            }
            if (tick.childCount == 0) remove(tick)
            // An oversized packet may empty the active stream; future packets still need its node.
            if (stream.childCount == 0 && stream !== retainedStream) remove(stream)
        }
    }

    internal fun clear(retainedStream: AbstractMutableTreeTableNode?) {
        while (root.childCount > 0) {
            val stream = root.getChildAt(0) as AbstractMutableTreeTableNode
            model.removeNodeFromParent(stream)
            while (stream.childCount > 0) stream.remove(0)
        }
        rowCount = 0
        if (retainedStream != null) {
            while (retainedStream.childCount > 0) retainedStream.remove(0)
            insert(retainedStream, root)
        }
    }

    private fun remove(node: AbstractMutableTreeTableNode) {
        rowCount -= count(node)
        model.removeNodeFromParent(node)
    }

    private fun count(node: TreeTableNode): Int = 1 + (0 until node.childCount).sumOf { count(node.getChildAt(it)) }
}
