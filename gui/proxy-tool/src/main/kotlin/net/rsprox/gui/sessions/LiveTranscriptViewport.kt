package net.rsprox.gui.sessions

import org.jdesktop.swingx.JXTreeTable
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

/** Keeps model updates from being mistaken for user scrolling. */
internal class LiveTranscriptViewport(
    private val table: JXTreeTable,
    private val scrollPane: JScrollPane,
) {
    private var followingTail = true
    private var updatePending = false
    private var previousMaximum = -1
    private var previousExtent = -1

    init {
        scrollPane.verticalScrollBar.addAdjustmentListener { event ->
            val model = scrollPane.verticalScrollBar.model
            val resized = model.maximum != previousMaximum || model.extent != previousExtent
            previousMaximum = model.maximum
            previousExtent = model.extent
            if (!updatePending && !resized && !event.valueIsAdjusting) {
                followingTail = model.value + model.extent >= model.maximum
            }
        }
        table.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    if (!updatePending && followingTail) scrollToBottom()
                }
            },
        )
    }

    internal fun followTail() {
        followingTail = true
        beforeUpdate()
    }

    internal fun beforeUpdate() {
        if (updatePending) return
        val position = scrollPane.viewport.viewPosition
        val row = table.rowAtPoint(position)
        val anchor = if (row >= 0) table.getPathForRow(row) else null
        val offset = if (row >= 0) position.y - table.getCellRect(row, 0, true).y else 0
        updatePending = true
        // One restoration per queued batch, after Swing has processed the model changes.
        SwingUtilities.invokeLater {
            try {
                if (followingTail) {
                    scrollToBottom()
                } else {
                    val anchorRow = if (anchor != null) table.getRowForPath(anchor) else -1
                    val y = if (anchorRow >= 0) table.getCellRect(anchorRow, 0, true).y + offset else 0
                    val maximum = (table.height - scrollPane.viewport.extentSize.height).coerceAtLeast(0)
                    scrollPane.viewport.viewPosition = Point(position.x, y.coerceIn(0, maximum))
                }
            } finally {
                updatePending = false
            }
        }
    }

    private fun scrollToBottom() {
        if (table.rowCount > 0) {
            table.scrollRectToVisible(table.getCellRect(table.rowCount - 1, 0, true))
        }
    }
}
