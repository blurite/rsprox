package net.rsprox.gui.sessions

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatScrollPane
import com.formdev.flatlaf.extras.components.FlatToolBar
import com.formdev.flatlaf.util.ColorFunctions
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.property.OmitFilteredPropertyTreeFormatter
import net.rsprox.shared.property.Property
import net.rsprox.shared.property.PropertyFormatterCollection
import net.rsprox.shared.property.RootProperty
import net.rsprox.shared.property.isExcluded
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
import java.awt.FlowLayout
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.text.SimpleDateFormat
import java.util.concurrent.ForkJoinPool
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollBar
import javax.swing.SwingUtilities
import kotlin.time.measureTime

public class SessionPanel(
    private val type: SessionType,
    private val sessionsPanel: SessionsPanel,
) : JPanel() {
    private val treeTable = JXTreeTable()
    private val tableModel = DefaultTreeTableModel()
    private val root = DefaultMutableTreeTableNode("Root")
    private val scrollPane: FlatScrollPane
    public var metrics: SessionMetrics = SessionMetrics()
    private var paused = false
    private var streamNode: StreamTreeTableNode? = null
    private var tickNode: TickTreeTableNode? = null
    public val isActive: Boolean
        get() = streamNode != null
    private var portNumber: Int = -1
    private var jumpToBottom: Boolean = true
    private var scrollbarMax: Int = -1

    init {
        layout = BorderLayout()

        tableModel.setColumnIdentifiers(listOf("Protocol", "Content"))
        tableModel.setRoot(root)

        treeTable.treeTableModel = tableModel

        treeTable.isEditable = false
        treeTable.rowSelectionAllowed = true
        treeTable.isDoubleBuffered = true
        treeTable.isColumnControlVisible = true
        treeTable.isLargeModel = true
        treeTable.searchable = TableSearchable(treeTable)
        treeTable.autoResizeMode = JXTreeTable.AUTO_RESIZE_LAST_COLUMN

        treeTable.tableHeader.resizingColumn = treeTable.columnModel.getColumn(1)

        treeTable.columnModel.getColumn(0).apply {
            minWidth = 150
            maxWidth = 250
            preferredWidth = 200
            width = preferredWidth
        }

        val highlighter = ColorHighlighter(HighlightPredicate.ODD, getOddRowColor(), null)
        treeTable.addHighlighter(highlighter)

        treeTable.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    if (jumpToBottom) {
                        treeTable.scrollRectToVisible(treeTable.getCellRect(treeTable.getRowCount() - 1, 0, true))
                    }
                }
            },
        )

        treeTable.addPropertyChangeListener { evt ->
            if ("UI" == evt.propertyName) {
                highlighter.background = getOddRowColor()
                treeTable.repaint()
            }
        }

        treeTable.expandAll()
        scrollPane =
            FlatScrollPane().apply {
                border = BorderFactory.createEmptyBorder()
                viewportBorder = BorderFactory.createEmptyBorder()
                verticalScrollBar.unitIncrement = 16
                setViewportView(treeTable)
            }
        scrollPane.verticalScrollBar.addAdjustmentListener { event ->
            val scrollBar = event.adjustable as JScrollBar
            val maximum = scrollBar.model.maximum
            if (scrollbarMax == -1 || scrollbarMax != maximum) {
                scrollbarMax = maximum
                return@addAdjustmentListener
            }
            if (event.valueIsAdjusting) {
                return@addAdjustmentListener
            }
            val extent = scrollBar.model.extent
            jumpToBottom = extent + event.value == maximum
        }
        add(scrollPane, BorderLayout.CENTER)

        val toolbar = FlatToolBar()
        toolbar.layout = FlowLayout(FlowLayout.CENTER)
        toolbar.isFloatable = false
        toolbar.isRollover = true

        // pause and resume buttons
        val pauseButton = FlatButton()
        pauseButton.buttonType = FlatButton.ButtonType.toolBarButton
        pauseButton.icon = AppIcons.Pause
        pauseButton.selectedIcon = AppIcons.Resume
        pauseButton.isSelected = paused
        pauseButton.toolTipText = if (paused) "Resume" else "Pause"
        pauseButton.addActionListener {
            paused = !paused
            pauseButton.isSelected = paused
            pauseButton.toolTipText = if (paused) "Resume" else "Pause"
        }
        toolbar.add(pauseButton)

        val clearAllButton = FlatButton()
        clearAllButton.buttonType = FlatButton.ButtonType.toolBarButton
        clearAllButton.icon = AppIcons.Delete
        clearAllButton.toolTipText = "Clear all"
        clearAllButton.addActionListener {
            scrollbarMax = -1
            jumpToBottom = true
            val model = treeTable.treeTableModel as DefaultTreeTableModel

            // Remove tick node content.
            streamNode?.let {
                while (it.childCount > 0) {
                    val tickNode = it.getChildAt(it.childCount - 1) as AbstractMutableTreeTableNode
                    model.removeNodeFromParent(tickNode)
                }
            }

            // Remove other stream node content.
            for (i in root.childCount - 1 downTo 0) {
                val node = root.getChildAt(i) as AbstractMutableTreeTableNode
                if (node === streamNode) continue
                model.removeNodeFromParent(node)
            }
            this@SessionPanel.tickNode = null
        }
        // make all buttons centered
        toolbar.add(clearAllButton)

        add(toolbar, BorderLayout.SOUTH)

        launchClient()
    }

    private fun launchClient() {
        ForkJoinPool.commonPool().submit {
            logger.info { "$type client thread: ${Thread.currentThread().name}" }
            val time =
                measureTime {
                    try {
                        portNumber =
                            when (type) {
                                SessionType.Java -> TODO()
                                SessionType.Native -> {
                                    App.service.launchNativeClient(UiSessionMonitor())
                                }
                                SessionType.RuneLite -> {
                                    App.service.launchRuneLiteClient(UiSessionMonitor())
                                }
                            }
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Unable to launch $type client (initialize git submodules, gradle refresh)"
                        }
                    }
                }
            logger.info { "$type client started in $time" }
        }
    }

    public fun notifyMetricsChanged() {
        sessionsPanel.syncSessionMetricsInfo(this)
    }

    private fun getOddRowColor(): Color {
        val background = treeTable.background
        val alternateRowColor =
            if (FlatLaf.isLafDark()) {
                ColorFunctions.lighten(background, 0.05f)
            } else {
                ColorFunctions.darken(background, 0.05f)
            }
        return alternateRowColor
    }

    private inner class UiSessionMonitor : SessionMonitor<BinaryHeader> {
        private var lastCycle = -1
        private val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.default(
                    SymbolDictionaryProvider.get(),
                ),
            )

        override fun onLogin(header: BinaryHeader) {
            // Update the session metrics data.
            metrics.worldName = "World ${header.worldId} (${header.worldActivity})"
            notifyMetricsChanged()

            // Create a new root node for the logged in session.
            SwingUtilities.invokeLater {
                val streamNode = StreamTreeTableNode(header)
                addNodeAndExpand(streamNode, root, root.childCount)
                this@SessionPanel.streamNode = streamNode
            }
        }

        override fun onLogout(header: BinaryHeader) {
            // Update the session metrics data.
            metrics.username = ""
            notifyMetricsChanged()

            // Clear the stream node.
            SwingUtilities.invokeLater {
                streamNode = null
            }
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
            if (metrics.userId != -1L && metrics.userHash != -1L) {
                App.service.updateCredentials(name, metrics.userId, metrics.userHash)
            }
        }

        override fun onUserInformationUpdate(
            userId: Long,
            userHash: Long,
        ) {
            metrics.userId = userId
            metrics.userHash = userHash
        }

        override fun onTranscribe(
            cycle: Int,
            property: RootProperty,
        ) {
            if (paused) return
            SwingUtilities.invokeLater {
                val tickNode = findOrCreateTickNode(cycle)
                createMessageNode(tickNode, cycle, property)
            }
        }

        private fun createMessageNode(
            tickNode: AbstractMutableTreeTableNode,
            cycle: Int,
            property: RootProperty,
        ) {
            val previewText = getPreviewText(property)
            val rootNode = MessageTreeTableNode(previewText, property.prot)
            addNodeAndExpand(rootNode, tickNode, tickNode.childCount)
            createMessageChildNodes(cycle, rootNode, property)
        }

        private fun getPreviewText(
            property: Property,
            indent: Int = 0,
        ): String {
            val children = property.children
            val previewProps = children.filter { it.children.isEmpty() }
            val previewText =
                if (previewProps.isNotEmpty()) {
                    buildString {
                        var count = 0
                        for (child in previewProps) {
                            if (child.isExcluded()) {
                                continue
                            }
                            val linePrefix = if (count++ == 0) null else ", "
                            formatter.writeChild(child, this@buildString, indent, linePrefix)
                        }
                    }
                } else {
                    ""
                }
            return previewText
        }

        private fun createMessageChildNodes(
            cycle: Int,
            parentNode: AbstractMutableTreeTableNode,
            rootProperty: RootProperty,
            property: Property = rootProperty,
            indent: Int = 0,
        ) {
            for (child in property.children) {
                if (child.isExcluded()) continue
                if (child.children.isEmpty()) continue // they get consumed in preview
                when (child) {
                    is GroupProperty -> {
                        val previewText = getPreviewText(child, indent)
                        val groupNode = MessageTreeTableNode(previewText, child.propertyName)
                        addNodeAndExpand(groupNode, parentNode, parentNode.childCount)
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
                this@SessionPanel.tickNode = tickNode
                addNodeAndExpand(tickNode, streamNode, streamNode.childCount)
            }
            return tickNode
        }
    }

    private fun addNodeAndExpand(
        newChild: AbstractMutableTreeTableNode,
        parent: AbstractMutableTreeTableNode,
        index: Int,
    ) {
        tableModel.insertNodeInto(newChild, parent, index)
        treeTable.expandRow(treeTable.rowCount - 1)
    }

    public fun kill() {
        if (portNumber == -1) return
        App.service.killAliveProcess(portNumber)
    }

    private companion object {
        private class StreamTreeTableNode(
            private val header: BinaryHeader,
        ) : SessionBaseTreeTableNode() {
            override fun getValueAt(column: Int) =
                when (column) {
                    0 -> "Stream"
                    1 ->
                        "${header.worldId} (${header.worldActivity}) at ${
                            SimpleDateFormat.getTimeInstance().format(header.timestamp)
                        }"

                    else -> error("Invalid column index: $column")
                }

            override fun getColumnCount() = 2
        }

        private class TickTreeTableNode(
            private val tickNumber: Int,
        ) : SessionBaseTreeTableNode() {
            override fun getValueAt(column: Int) =
                when (column) {
                    0 -> "Tick $tickNumber"
                    1 -> null
                    else -> error("Invalid column index: $column")
                }

            override fun getColumnCount() = 2
        }

        private class MessageTreeTableNode(
            private val message: String,
            private val prot: Any?,
        ) : SessionBaseTreeTableNode() {
            override fun getValueAt(column: Int) =
                when (column) {
                    0 -> prot?.toString()?.lowercase()
                    1 -> message
                    else -> error("Invalid column index: $column")
                }

            override fun getColumnCount() = 2
        }

        private abstract class SessionBaseTreeTableNode : AbstractMutableTreeTableNode(null, true)

        private val logger = InlineLogger()
    }
}
