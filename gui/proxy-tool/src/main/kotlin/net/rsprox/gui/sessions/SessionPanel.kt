package net.rsprox.gui.sessions

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatScrollPane
import com.formdev.flatlaf.extras.components.FlatToolBar
import com.formdev.flatlaf.util.ColorFunctions
import com.github.michaelbull.logging.InlineLogger
import net.rsprox.cache.api.CacheProvider
import net.rsprox.cache.api.type.ClientScriptDefinitionProvider
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.downloader.JagexNativeClientDownloader
import net.rsprox.proxy.rs3.Rs3SessionMonitor
import net.rsprox.proxy.rs3.gameval.Rs3GamevalLookup
import net.rsprox.shared.SessionMonitor
import net.rsprox.shared.account.JagexCharacter
import net.rsprox.shared.property.*
import net.rsprox.shared.property.regular.GroupProperty
import net.rsprox.shared.property.regular.ListProperty
import net.rsprox.shared.symbols.SymbolDictionaryProvider
import net.rsprox.transcriber.rs3.text.Rs3PropertyFormatter
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
    public val type: SessionType,
    private val sessionsPanel: SessionsPanel,
    character: JagexCharacter?,
) : JPanel() {
    private val treeTable = JXTreeTable()
    private val tableModel = DefaultTreeTableModel()
    private val root = DefaultMutableTreeTableNode("Root")
    private val scrollPane: FlatScrollPane
    public var metrics: SessionMetrics = SessionMetrics()
    private var paused = false
    private var streamNode: StreamTreeTableNode? = null
    private var tickNode: TickTreeTableNode? = null
    private var lastCycle = -1
    private var rs3Connected = false
    public val isActive: Boolean
        get() = if (type.isRs3) rs3Connected else streamNode != null
    private var portNumber: Int = -1
    private var jumpToBottom: Boolean = true
    private var scrollbarMax: Int = -1
    private val launchProgress = if (type.isRs3) Rs3LaunchProgressPanel() else null

    private var rs3ClientScripts = ClientScriptDefinitionProvider.EMPTY
    private val rs3Formatter =
        Rs3PropertyFormatter.create(App.service.settingsStore, Rs3GamevalLookup) { id ->
            rs3ClientScripts.getClientScriptDefinition(id)
        }

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
        launchProgress?.let { add(it, BorderLayout.NORTH) }

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
            val root = model.root as DefaultMutableTreeTableNode
            val streamNode = this@SessionPanel.streamNode

            // Clear the root node (always <= 1 entry - the streamNode)
            for (i in root.childCount - 1 downTo 0) {
                val node = root.getChildAt(i) as AbstractMutableTreeTableNode
                model.removeNodeFromParent(node)
            }

            if (streamNode != null) {

                // Replace the stream node directly instead of purging it one by one
                val oldHeader = streamNode.header
                val streamNode =
                    if (oldHeader !=
                        null
                    ) {
                        StreamTreeTableNode(oldHeader)
                    } else {
                        StreamTreeTableNode(streamNode.label ?: "")
                    }
                addNodeAndExpand(streamNode, root, root.childCount)
                this@SessionPanel.streamNode = streamNode
            }
            this@SessionPanel.tickNode = null
        }
        // make all buttons centered
        toolbar.add(clearAllButton)

        add(toolbar, BorderLayout.SOUTH)

        launchClient(character)
    }

    private fun launchClient(character: JagexCharacter?) {
        ForkJoinPool.commonPool().submit {
            logger.info { "$type client thread: ${Thread.currentThread().name}" }
            val time =
                measureTime {
                    try {
                        when (type) {
                            SessionType.Java -> TODO()
                            SessionType.Native -> {
                                portNumber = App.service.allocatePort()
                                val target = App.service.initializeHttpServer(portNumber)
                                App.service.launchNativeClient(
                                    UiSessionMonitor(),
                                    character,
                                    portNumber,
                                    target,
                                )
                            }

                            SessionType.RuneLite -> {
                                portNumber = App.service.allocatePort()
                                val target = App.service.initializeHttpServer(portNumber)
                                App.service.launchRuneLiteClient(
                                    UiSessionMonitor(),
                                    character,
                                    portNumber,
                                    target,
                                )
                            }

                            SessionType.RS3, SessionType.RS3_VULKAN -> {
                                val rs3SessionMonitor = Rs3SessionMonitor()
                                rs3SessionMonitor.stateListener = { state ->
                                    SwingUtilities.invokeLater {
                                        rs3ClientScripts = state.clientScripts
                                        val status = state.status
                                        val name = status?.name.orEmpty()
                                        val identityChanged =
                                            metrics.username != name ||
                                                metrics.userId != state.userId ||
                                                metrics.userHash != state.userHash
                                        rs3Connected = status != null
                                        metrics.username = status?.name?.ifBlank { "Connected" }.orEmpty()
                                        metrics.userId = state.userId
                                        metrics.userHash = state.userHash
                                        metrics.worldName =
                                            when {
                                                status == null -> ""
                                                status.world -> "World ${status.endpoint}"
                                                else -> "Lobby ${status.endpoint}"
                                            }
                                        metrics.bandInPerSec =
                                            state.incomingBytesPerSecond.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                                        metrics.bandOutPerSec =
                                            state.outgoingBytesPerSecond.coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
                                        if (identityChanged &&
                                            name.isNotBlank() &&
                                            state.userId != -1L &&
                                            state.userHash != -1L
                                        ) {
                                            App.service.updateCredentials(name, state.userId, state.userHash)
                                        }
                                        sessionsPanel.updateTabTitle(
                                            this@SessionPanel,
                                            status?.name?.ifBlank { "RuneScape 3" } ?: "RuneScape 3",
                                        )
                                        notifyMetricsChanged()
                                    }
                                }
                                rs3SessionMonitor.listener = { cycle, property ->
                                    if (!paused) {
                                        SwingUtilities.invokeLater {
                                            if (this@SessionPanel.streamNode == null) {
                                                val newStreamNode = StreamTreeTableNode("RS3 Session")
                                                addNodeAndExpand(newStreamNode, root, root.childCount)
                                                this@SessionPanel.streamNode = newStreamNode
                                            }
                                            val tickNode = findOrCreateTickNode(cycle)
                                            createMessageNode(tickNode, cycle, property, rs3Formatter)
                                        }
                                    }
                                }
                                val handle =
                                    App.service.launchRs3Client(
                                        rs3SessionMonitor,
                                        character,
                                        upstreamJavConfigUrl =
                                            if (type == SessionType.RS3_VULKAN) {
                                                JagexNativeClientDownloader.VULKAN_RS3_JAV_CONFIG_URL
                                            } else {
                                                JagexNativeClientDownloader.DEFAULT_RS3_JAV_CONFIG_URL
                                            },
                                        onProgress = { launchProgress?.update(it) },
                                    )
                                portNumber = handle.port
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) {
                            "Unable to launch $type client"
                        }
                        launchProgress?.finish(success = false)
                        return@submit
                    }
                }
            logger.info { "$type client started in $time" }
            launchProgress?.finish(success = true)
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

    private fun createMessageNode(
        tickNode: AbstractMutableTreeTableNode,
        cycle: Int,
        property: RootProperty,
        formatter: OmitFilteredPropertyTreeFormatter,
    ) {
        val previewText = getPreviewText(property, formatter)
        val rootNode = MessageTreeTableNode(previewText, property.prot)
        addNodeAndExpand(rootNode, tickNode, tickNode.childCount)
        createMessageChildNodes(cycle, rootNode, property, formatter)
    }

    private fun getPreviewText(
        property: Property,
        formatter: OmitFilteredPropertyTreeFormatter,
        indent: Int = 0,
    ): String {
        val children = property.children
        // Groups are rows even when empty; formatting them inline appends their label to the parent.
        val previewProps = children.filter { it !is GroupProperty && it.children.isEmpty() }
        val previewText =
            if (previewProps.isNotEmpty()) {
                val lines = mutableListOf<String>()
                val builder = StringBuilder()
                var count = 0
                for (child in previewProps) {
                    if (child.isExcluded()) {
                        continue
                    }
                    val linePrefix = if (count++ == 0) null else ", "
                    formatter.writeChild(child, builder, lines, indent, linePrefix)
                }
                lines.add(builder.toString())
                lines.joinToString(separator = System.lineSeparator())
            } else {
                ""
            }
        return previewText
    }

    private fun createMessageChildNodes(
        cycle: Int,
        parentNode: AbstractMutableTreeTableNode,
        rootProperty: RootProperty,
        formatter: OmitFilteredPropertyTreeFormatter,
        property: Property = rootProperty,
        indent: Int = 0,
    ) {
        for (child in property.children) {
            if (child.isExcluded()) continue
            if (child !is GroupProperty && child.children.isEmpty()) continue
            when (child) {
                is GroupProperty -> {
                    val previewText = getPreviewText(child, formatter, indent)
                    val groupNode = MessageTreeTableNode(previewText, child.propertyName)
                    addNodeAndExpand(groupNode, parentNode, parentNode.childCount)
                    createMessageChildNodes(cycle, groupNode, rootProperty, formatter, child, indent + 1)
                }

                is ListProperty -> {
                    val previewText = getPreviewText(child, formatter, indent)
                    val groupNode = MessageTreeTableNode(previewText, child.propertyName)
                    addNodeAndExpand(groupNode, parentNode, parentNode.childCount)
                }

                else -> {
                    error("Unsupported property with children. Property type: ${child::class.simpleName}")
                }
            }
        }
    }

    private inner class UiSessionMonitor : SessionMonitor<BinaryHeader> {
        private var lastCacheProvider: CacheProvider? = null
        private val formatter =
            OmitFilteredPropertyTreeFormatter(
                PropertyFormatterCollection.default(
                    SymbolDictionaryProvider.get(),
                    App.service.settingsStore,
                ) { this.lastCacheProvider?.get() ?: error("Cache unavailable") },
            )

        override fun onCacheUpdate(cacheProvider: CacheProvider) {
            this.lastCacheProvider = cacheProvider
        }

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
                createMessageNode(tickNode, cycle, property, formatter)
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
            addNodeAndExpand(tickNode, streamNode, streamNode.childCount)
        }
        return tickNode
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
        private class StreamTreeTableNode private constructor(
            val header: BinaryHeader?,
            val label: String?,
        ) : SessionBaseTreeTableNode() {
            constructor(header: BinaryHeader) : this(header, null)

            constructor(label: String) : this(null, label)

            override fun getValueAt(column: Int) =
                when (column) {
                    0 -> "Stream"
                    1 -> {
                        val h = header
                        if (h != null) {
                            "${h.worldId} (${h.worldActivity}) at ${
                                SimpleDateFormat.getTimeInstance().format(h.timestamp)
                            }"
                        } else {
                            label ?: ""
                        }
                    }

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
