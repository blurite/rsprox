package net.rsprox.gui.sessions

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.components.FlatScrollPane
import com.formdev.flatlaf.util.ColorFunctions
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.gui.App
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.progressbar.ProgressBarNotifier
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.*
import net.rsprox.shared.property.regular.GroupProperty
import net.rsprox.shared.property.regular.ListProperty
import net.rsprox.shared.symbols.SymbolDictionaryProvider
import org.jdesktop.swingx.JXTreeTable
import org.jdesktop.swingx.decorator.ColorHighlighter
import org.jdesktop.swingx.decorator.HighlightPredicate
import org.jdesktop.swingx.search.TableSearchable
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultTreeTableModel
import java.awt.BorderLayout
import java.awt.Color
import java.text.SimpleDateFormat
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

        tableModel.setColumnIdentifiers(listOf("#", "Content"))
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

        treeTable.tableHeader.resizingColumn = treeTable.columnModel.getColumn(1)

        treeTable.columnModel.getColumn(0).apply {
            preferredWidth = 150
            minWidth = 150
            maxWidth = 250
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

    private fun getOddRowColor(): Color {
        val background = treeTable.background
        val alternateRowColor = if (FlatLaf.isLafDark())
            ColorFunctions.lighten(background, 0.05f)
        else
            ColorFunctions.darken(background, 0.05f)
        return alternateRowColor
    }

    private inner class UiSessionMonitor : SessionMonitor<BinaryHeader> {

        private var streamNode: StreamTreeTableNode? = null
        private var tickNode: TickTreeTableNode? = null
        private var lastCycle = -1
        private val formatter = OmitFilteredPropertyTreeFormatter(
            PropertyFormatterCollection.default(
                SymbolDictionaryProvider.get()
            )
        )

        override fun onLogin(header: BinaryHeader) {
            // Update the session metrics data.
            metrics.worldName = "World ${header.worldId} (${header.worldActivity})"
            notifyMetricsChanged()

            // Create a new root node for the logged in session.
            SwingUtilities.invokeLater {
                val streamNode = StreamTreeTableNode(header)
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
            metrics.bandInPerSec = bytesPerLastSecond.toInt()
            notifyMetricsChanged()
        }

        override fun onOutgoingBytesPerSecondUpdate(bytesPerLastSecond: Long) {
            metrics.bandOutPerSec = bytesPerLastSecond.toInt()
            notifyMetricsChanged()
        }

        override fun onNameUpdate(name: String) {
            metrics.username = name
            sessionsPanel.updateTabTitle(this@SessionPanel, name)
            notifyMetricsChanged()
        }

        override fun onTranscribe(cycle: Int, property: RootProperty<*>) {
            val tickNode = findOrCreateTickNode(cycle)
            createMessageNode(tickNode, cycle, property)
        }

        private fun createMessageNode(
            tickNode: AbstractMutableTreeTableNode,
            cycle: Int,
            property: RootProperty<*>
        ) {
            val previewText = getPreviewText(property)
            val rootNode = MessageTreeTableNode(cycle, previewText, property, property.prot)
            tableModel.insertNodeInto(rootNode, tickNode, tickNode.childCount)
            createMessageChildNodes(cycle, rootNode, property)
        }

        private fun getPreviewText(property: Property, indent: Int = 0): String {
            val children = property.children
            val previewProps = children.filter { it.children.isEmpty() }
            val previewText = if (previewProps.isNotEmpty()) {
                buildString {
                    for (child in previewProps) {
                        val linePrefix = if (child === previewProps.first()) null else ", "
                        formatter.writeChild(child, this@buildString, indent, linePrefix)
                    }
                }
            } else {
                if (property is ChildProperty<*>) {
                    property.propertyName
                } else {
                    ""
                }
            }
            return previewText
        }

        private fun createMessageChildNodes(
            cycle: Int,
            parentNode: AbstractMutableTreeTableNode,
            rootProperty: RootProperty<*>,
            property: Property = rootProperty,
            indent: Int = 0
        ) {
            for (child in property.children) {
                if (child.children.isEmpty()) continue // they get consumed in preview
                val previewText = getPreviewText(child, indent)
                when (child) {
                    is GroupProperty -> {
                        val groupNode = MessageTreeTableNode(cycle, previewText, rootProperty, null)
                        tableModel.insertNodeInto(groupNode, parentNode, parentNode.childCount)
                        createMessageChildNodes(cycle, groupNode, rootProperty, child, indent + 1)
                    }

                    is ListProperty -> {

                    }

                    else -> {
                        error("Unsupported property with children. Property type: ${child::class.simpleName}")
                    }
                }
            }

        }

        private fun findOrCreateTickNode(tickNumber: Int): AbstractMutableTreeTableNode {
            val streamNode = streamNode!!
            var tickNode = tickNode
            if (tickNode == null || lastCycle != tickNumber) {
                lastCycle = tickNumber
                tickNode = TickTreeTableNode(tickNumber)
                this.tickNode = tickNode
                tableModel.insertNodeInto(tickNode, streamNode, streamNode.childCount)
            }
            return tickNode
        }
    }

    private companion object {
        private class StreamTreeTableNode(
            private val header: BinaryHeader
        ) : SessionBaseTreeTableNode() {

            override fun getValueAt(column: Int) = when (column) {
                0 -> "Stream"
                1 -> "${header.worldId} (${header.worldActivity}) at ${
                    SimpleDateFormat.getTimeInstance().format(header.timestamp)
                }"

                else -> error("Invalid column index: $column")
            }

            override fun getColumnCount() = 2
        }

        private class TickTreeTableNode(
            private val tickNumber: Int
        ) : SessionBaseTreeTableNode() {

            override fun getValueAt(column: Int) = when (column) {
                0 -> "Tick $tickNumber"
                1 -> null
                else -> error("Invalid column index: $column")
            }

            override fun getColumnCount() = 2
        }

        private class MessageTreeTableNode(
            private val tick: Int,
            private val message: String,
            private val rootProperty: RootProperty<*>,
            private val prot: Any?
        ) : SessionBaseTreeTableNode() {

            override fun getValueAt(column: Int) = when (column) {
                0 -> prot
                1 -> message
                else -> error("Invalid column index: $column")
            }

            override fun getColumnCount() = 2
        }

        private abstract class SessionBaseTreeTableNode() : AbstractMutableTreeTableNode(null, true)

        private val logger = InlineLogger()
    }
}
