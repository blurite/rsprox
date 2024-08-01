package net.rsprox.gui.sessions

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.components.FlatScrollPane
import com.formdev.flatlaf.util.ColorFunctions
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.gui.App
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.progressbar.ProgressBarNotifier
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.RootProperty
import org.jdesktop.swingx.JXTreeTable
import org.jdesktop.swingx.decorator.ColorHighlighter
import org.jdesktop.swingx.decorator.HighlightPredicate
import org.jdesktop.swingx.search.TableSearchable
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultTreeTableModel
import java.awt.BorderLayout
import java.awt.Color
import java.util.concurrent.ForkJoinPool
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.time.measureTime

public class SessionPanel(
    private val type: SessionType,
    private val app: App,
    private val sessionsPanel: SessionsPanel
) : JPanel() {

    private var scrolledToLatest: Boolean = false
    private val treeTable = JXTreeTable()
    private val tableModel = DefaultTreeTableModel()
    private val root = DefaultMutableTreeTableNode("Root")
    private val scrollPane: FlatScrollPane
    public var metrics: SessionMetrics = SessionMetrics()

    init {
        layout = BorderLayout()

        tableModel.setColumnIdentifiers(listOf("ID", "Tick", "Message"))
        tableModel.setRoot(root)

        treeTable.treeTableModel = tableModel
        treeTable.setLeafIcon(null)
        treeTable.setOpenIcon(null)
        treeTable.setClosedIcon(null)
        treeTable.isEditable = false
        treeTable.rowSelectionAllowed = true
        treeTable.isDoubleBuffered = true
        treeTable.isColumnControlVisible = true
        treeTable.isLargeModel = true
        treeTable.searchable = TableSearchable(treeTable)
        treeTable.autoResizeMode = JXTreeTable.AUTO_RESIZE_LAST_COLUMN

        treeTable.tableHeader.resizingColumn = treeTable.columnModel.getColumn(2)

        treeTable.columnModel.getColumn(0).apply {
            preferredWidth = 100
            minWidth = 100
            maxWidth = 200
        }


        treeTable.columnModel.getColumn(1).apply {
            preferredWidth = 50
            minWidth = 50
            maxWidth = 100
        }
        val highlighter = ColorHighlighter(HighlightPredicate.ODD, getOddRowColor(), null)
        treeTable.addHighlighter(highlighter)

        addPropertyChangeListener { evt ->
            if ("lookAndFeel" == evt.propertyName) {
                highlighter.background = getOddRowColor()
                treeTable.repaint()
            }
        }

        treeTable.expandAll()
        scrollPane = FlatScrollPane().apply {
            border = BorderFactory.createEmptyBorder()
            viewportBorder = BorderFactory.createEmptyBorder()
            setViewportView(treeTable)
            verticalScrollBar.model.addChangeListener {
                val model = it.source as javax.swing.BoundedRangeModel
                scrolledToLatest = model.value == model.maximum - verticalScrollBar.visibleAmount
            }
        }
        add(scrollPane, BorderLayout.CENTER)

        launchClient()
    }

    private fun launchClient() {
        ForkJoinPool.commonPool().submit {
            logger.info { "Native client thread: ${Thread.currentThread().name}" }
            val time = measureTime {
                val notifier = ProgressBarNotifier { percentage, text ->
                    logger.info { "Native client progress: $percentage% - $text" }
                }
                app.service.launchNativeClient(notifier, UiSessionMonitor())
            }
            logger.info { "Native client started in $time" }
        }
    }

    public fun notifyMetricsChanged() {
        sessionsPanel.syncSessionMetricsInfo(this)
    }

    private fun createNode(id: Int, tick: Int, message: String): MessageTreeTableNode {
        return MessageTreeTableNode(id, tick, message)
    }

    private fun createStreamNode(): DefaultMutableTreeTableNode {
        return DefaultMutableTreeTableNode("Stream", true)
    }

    private fun createTickNode(tickNumber: Int): DefaultMutableTreeTableNode {
        return DefaultMutableTreeTableNode("Tick $tickNumber", true)
    }

    private fun getOddRowColor(): Color {
        val background = treeTable.background
        val alternateRowColor = if (FlatLaf.isLafDark())
            ColorFunctions.lighten(background, 0.05f)
        else
            ColorFunctions.darken(background, 0.05f)
        return alternateRowColor
    }

    private inner class UiSessionMonitor : SessionMonitor<BinaryHeader> {

        private var streamNode: DefaultMutableTreeTableNode? = null
        private var tickNode: DefaultMutableTreeTableNode? = null
        private var lastCycle = -1
        private var id = 0

        override fun onLogin(header: BinaryHeader) {
            // Update the session metrics data.
            metrics.worldName = "World ${header.worldId} (${header.worldActivity})"
            notifyMetricsChanged()

            // Create a new root node for the logged in session.
            SwingUtilities.invokeLater {
                val streamNode = createStreamNode()
                tableModel.insertNodeInto(streamNode, root, root.childCount)
                this.streamNode = streamNode
            }
        }

        override fun onLogout(header: BinaryHeader) {
            // Update the session metrics data.
            metrics.username = ""
            notifyMetricsChanged()

            // Clear the stream node.
            streamNode = null
        }

        override fun onIncomingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        }

        override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
        }

        override fun onNameUpdate(name: String) {
            metrics.username = name
            sessionsPanel.updateTabTitle(this@SessionPanel,name)
            notifyMetricsChanged()
        }

        override fun onTranscribe(cycle: Int, property: RootProperty<*>) {
//            val streamNode = streamNode
//                ?: error("Stream node is null")
//            SwingUtilities.invokeLater {
//                val tickNode: DefaultMutableTreeTableNode
//                if (cycle != lastCycle) {
//                    tickNode = createTickNode(cycle)
//                    tableModel.insertNodeInto(tickNode, streamNode, streamNode.childCount)
//                    this.tickNode = tickNode
//                    lastCycle = cycle
//                } else {
//                    tickNode = this.tickNode
//                        ?: error("Tick node is null")
//                }
//                tableModel.insertNodeInto(createNode(id++, cycle, message), tickNode, tickNode.childCount)
//                val row = treeTable.rowCount - 1
//                treeTable.expandRow(row)
//                if (scrolledToLatest) {
//                    scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
//                }
//            }
        }
    }

    private companion object {
        private class MessageTreeTableNode(
            private val id: Int,
            private val tick: Int,
            private val message: String
        ) : AbstractMutableTreeTableNode() {

            override fun getValueAt(column: Int) = when (column) {
                0 -> id
                1 -> tick
                2 -> message
                else -> error("Invalid column index: $column")
            }

            override fun getColumnCount() = 3
        }

        val logger = InlineLogger()
    }
}
