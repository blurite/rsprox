package net.rsprox.gui

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatComboBox
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatScrollPane
import com.formdev.flatlaf.extras.components.FlatToggleButton
import com.formdev.flatlaf.util.ColorFunctions
import com.formdev.flatlaf.util.SystemFileChooser
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.sessions.SessionType
import net.rsprox.proxy.binary.BinaryHeader
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.preferences.ClientPreferencesFile
import net.rsprox.proxy.replay.ReplayPlaybackSnapshot
import net.rsprox.proxy.replay.ReplayPlaybackState
import net.rsprox.proxy.replay.ReplaySession
import net.rsprox.proxy.replay.ReplayTranscript
import net.rsprox.proxy.replay.ReplayTranscriptEntry
import net.rsprox.proxy.replay.ReplayTranscriptNode
import net.rsprox.proxy.target.ProxyTarget
import net.rsprox.proxy.util.OperatingSystem
import org.jdesktop.swingx.JXTreeTable
import org.jdesktop.swingx.decorator.ColorHighlighter
import org.jdesktop.swingx.decorator.ComponentAdapter
import org.jdesktop.swingx.decorator.HighlightPredicate
import org.jdesktop.swingx.search.TableSearchable
import org.jdesktop.swingx.treetable.AbstractMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode
import org.jdesktop.swingx.treetable.DefaultTreeTableModel
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.nio.file.Path
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.ForkJoinPool
import javax.swing.BorderFactory
import javax.swing.DefaultComboBoxModel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JSlider
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.UIManager
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.roundToInt

public class ReplayPanel(
    private val onReplayLoaded: (Path) -> Unit = {},
) : JPanel() {
    private var replaySession: ReplaySession? = null
    private var selectedPath: Path? = null
    private val titleLabel = FlatLabel()
    private val detailsLabel = FlatLabel()
    private val statusLabel = FlatLabel()
    private val tickLabel = FlatLabel()
    private val frameLabel = FlatLabel()
    private val loadingProgress = JProgressBar()
    private val timelineSlider = JSlider(0, 0, 0)
    private val speedButtons = linkedMapOf<Double, FlatButton>()
    private var selectedSpeed = 1.0
    private val tickSpinnerModel = SpinnerNumberModel(0, 0, 0, 1)
    private val tickSpinner = JSpinner(tickSpinnerModel)
    private val jumpStartButton = FlatButton()
    private val rewindButton = FlatButton()
    private val stepBackButton = FlatButton()
    private val playButton = FlatButton()
    private val stopButton = FlatButton()
    private val stepForwardButton = FlatButton()
    private val fastForwardButton = FlatButton()
    private val openButton = FlatButton()
    private val launchButton = FlatButton()
    private val followTranscriptButton = FlatToggleButton()
    private val retranscribeButton = FlatButton()
    private val transcriptOffsetSpinnerModel = SpinnerNumberModel(0, -120, 120, 1)
    private val transcriptOffsetSpinner = JSpinner(transcriptOffsetSpinnerModel)
    private val replayLaunchTypes =
        arrayOf(SessionType.RuneLite, SessionType.Native)
            .filter { type ->
                App.service.operatingSystem != OperatingSystem.MAC || type != SessionType.Native
            }.toTypedArray()
    private val replayLaunchTypeModel = DefaultComboBoxModel(replayLaunchTypes)
    private val replayLaunchTypeDropdown = FlatComboBox<SessionType>()
    private val replayTree = JXTreeTable()
    private val replayTreeModel = DefaultTreeTableModel()
    private val replayRoot = DefaultMutableTreeTableNode("Root")
    private val replayTickNodes = linkedMapOf<Int, TickTreeTableNode>()
    private var replayTranscript: ReplayTranscript? = null
    private var transcriptDisplayMode: TranscriptDisplayMode? = null
    private var liveTranscriptCenterTick: Int? = null
    private var updatingSlider = false
    private var userAdjustingTimelineSlider = false
    private var updatingTickSpinner = false
    private var loadingReplay = false
    private var retranscribingReplay = false
    private var launchingReplayClient = false
    private var loadGeneration = 0
    private var replayStatusText = ""
    private var highlightedTick: Int? = null
    private var lastScrolledTick: Int? = null

    init {
        layout = BorderLayout()
        border = BorderFactory.createEmptyBorder()
        replayTreeModel.setColumnIdentifiers(listOf("Protocol", "Content"))
        replayTreeModel.setRoot(replayRoot)
        replayTree.treeTableModel = replayTreeModel
        add(createContent(), BorderLayout.CENTER)
        add(createTimelineBar(), BorderLayout.SOUTH)

        timelineSlider.addChangeListener {
            if (updatingSlider ||
                userAdjustingTimelineSlider ||
                timelineSlider.valueIsAdjusting
            ) {
                return@addChangeListener
            }
            seekToTick(timelineSlider.value)
        }
        timelineSlider.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    userAdjustingTimelineSlider = true
                }

                override fun mouseReleased(e: MouseEvent) {
                    userAdjustingTimelineSlider = false
                    if (timelineSlider.isEnabled) {
                        seekToTick(timelineSlider.value)
                    }
                }
            },
        )
        tickSpinner.addChangeListener {
            if (updatingTickSpinner) return@addChangeListener
            val tick = (tickSpinner.value as Number).toInt()
            seekToTick(tick)
        }
        transcriptOffsetSpinner.addChangeListener {
            renderTranscriptForCurrentState(force = true)
        }

        Timer(250) {
            refreshControls()
        }.start()
        updateEmptyState()
        refreshControls()
    }

    public fun openReplayFile(path: Path) {
        val generation = ++loadGeneration
        SwingUtilities.invokeLater {
            if (generation != loadGeneration) {
                return@invokeLater
            }
            replaySession?.close()
            replaySession = null
            launchingReplayClient = false
            selectedPath = path
            clearReplayTree()
            replayTranscript = null
            transcriptDisplayMode = null
            liveTranscriptCenterTick = null
            updateLoadingState(path)
            refreshControls()
        }
        ForkJoinPool.commonPool().submit {
            var session: ReplaySession? = null
            try {
                session = App.service.loadReplaySession(path)
                val transcript = App.service.transcribeReplaySession(session)
                SwingUtilities.invokeLater {
                    if (generation != loadGeneration) {
                        session.close()
                        return@invokeLater
                    }
                    loadingReplay = false
                    launchingReplayClient = false
                    loadingProgress.isVisible = false
                    replaySession = session
                    selectedPath = path
                    onReplayLoaded(path)
                    val windowMode = session.timeline.windowMode
                    if (windowMode != -1) {
                        updateInitialWindowMode(windowMode, session.timeline.header.revision)
                    }
                    updateLoadedMetadata(transcript)
                    refreshControls()
                }
            } catch (t: Throwable) {
                session?.close()
                SwingUtilities.invokeLater {
                    if (generation != loadGeneration) {
                        return@invokeLater
                    }
                    loadingReplay = false
                    launchingReplayClient = false
                    loadingProgress.isVisible = false
                    selectedPath = null
                    updateEmptyState()
                    refreshControls()
                    JOptionPane.showMessageDialog(
                        this,
                        t.message ?: "Unable to load replay file.",
                        "Replay Load Failed",
                        JOptionPane.ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun updateInitialWindowMode(
        windowMode: Int,
        gameRevision: Int,
    ) {
        val preferences =
            ClientPreferencesFile
                .loadOrDefaultCustom()
                .copy(windowMode = windowMode)

        ClientPreferencesFile.write(preferences, gameRevision)
    }

    public fun closeReplay() {
        loadGeneration++
        loadingReplay = false
        retranscribingReplay = false
        launchingReplayClient = false
        loadingProgress.isVisible = false
        replaySession?.close()
        replaySession = null
        selectedPath = null
        replayTranscript = null
        transcriptDisplayMode = null
        liveTranscriptCenterTick = null
        clearReplayTree()
        updateEmptyState()
        refreshControls()
    }

    private fun createContent(): JPanel {
        return JPanel(MigLayout("ins 24 28 16 28, gap 16, fill", "[fill, grow]", "[][fill, grow]")).apply {
            add(createHeader(), "growx, wrap")
            add(createReplayTreePanel(), "grow, push")
        }
    }

    private fun createHeader(): JPanel {
        return JPanel(MigLayout("ins 0, gap 10 2, fillx, hidemode 3", "[fill, grow][]", "[][]")).apply {
            isOpaque = false
            add(
                titleLabel.apply {
                    text = "Replay"
                    putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxAccent")
                    font = font.deriveFont(font.size2D + 4f)
                },
                "growx",
            )
            add(createHeaderActions(), "right, wrap")
            add(detailsLabel.apply { putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxMuted") }, "growx")
            add(createHeaderStatus(), "right")
        }
    }

    private fun createHeaderActions(): JPanel {
        return JPanel(MigLayout("ins 0, gap 8", "[][][]", "[]")).apply {
            isOpaque = false
            add(
                openButton.apply {
                    text = "Open"
                    icon = AppIcons.Add
                    addActionListener { chooseReplayFile() }
                },
            )
            add(
                replayLaunchTypeDropdown.apply {
                    model = replayLaunchTypeModel
                    selectedItem = SessionType.RuneLite
                    toolTipText = "Replay Client Type"
                },
                "w 120!",
            )
            add(
                launchButton.apply {
                    text = "Launch"
                    icon = AppIcons.Run
                    addActionListener { launchReplayClient() }
                },
            )
        }
    }

    private fun createHeaderStatus(): JPanel {
        return JPanel(MigLayout("ins 0, gap 8, hidemode 3", "[][]", "[]")).apply {
            isOpaque = false
            add(
                loadingProgress.apply {
                    isIndeterminate = true
                    isStringPainted = false
                    isVisible = false
                    putClientProperty("JProgressBar.paintBorder", false)
                },
                "w 92!, h 8!",
            )
            add(
                statusLabel.apply {
                    putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxMuted")
                },
            )
        }
    }

    private fun createReplayTreePanel(): JPanel {
        replayTree.apply {
            isEditable = false
            rowSelectionAllowed = true
            isDoubleBuffered = true
            isColumnControlVisible = true
            isLargeModel = true
            searchable = TableSearchable(this)
            autoResizeMode = JXTreeTable.AUTO_RESIZE_LAST_COLUMN
            tableHeader.reorderingAllowed = false
            rowHeight = 24
            columnModel.getColumn(0).apply {
                minWidth = 150
                maxWidth = 250
                preferredWidth = 200
                width = preferredWidth
            }
        }
        val highlighter = ColorHighlighter(HighlightPredicate.ODD, getOddRowColor(), null)
        val currentTickHighlighter =
            ColorHighlighter(
                CurrentTickHighlightPredicate(),
                getCurrentTickRowColor(),
                null,
            )
        replayTree.addHighlighter(highlighter)
        replayTree.addHighlighter(currentTickHighlighter)
        replayTree.addPropertyChangeListener { evt ->
            if ("UI" == evt.propertyName) {
                highlighter.background = getOddRowColor()
                currentTickHighlighter.background = getCurrentTickRowColor()
                replayTree.repaint()
            }
        }
        return JPanel(MigLayout("ins 0, gap 0, fill", "[fill, grow]", "[][fill, grow]")).apply {
            add(
                JPanel(MigLayout("ins 0 0 8 0, gap 8, fillx", "[][fill, grow][]", "[]")).apply {
                    isOpaque = false
                    add(
                        FlatLabel().apply {
                            text = "Transcript"
                            putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxAccent")
                            font = font.deriveFont(font.size2D + 2f)
                        },
                    )
                    add(createTranscriptControls())
                    add(
                        frameLabel.apply {
                            putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxMuted")
                        },
                        "right",
                    )
                },
                "growx, wrap",
            )
            add(
                FlatScrollPane().apply {
                    border = BorderFactory.createEmptyBorder()
                    viewportBorder = BorderFactory.createEmptyBorder()
                    verticalScrollBar.unitIncrement = 16
                    setViewportView(replayTree)
                },
                "grow, push",
            )
        }
    }

    private fun createTimelineBar(): JPanel {
        return JPanel(MigLayout("ins 10 18 12 18, gap 8, fillx", "[fill, grow]", "[][][][]")).apply {
            border = BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color(0, 0, 0, 45))
            add(
                JPanel(MigLayout("ins 0, gap 8, fillx", "[fill, grow][]", "[]")).apply {
                    isOpaque = false
                    add(tickLabel, "growx")
                    add(
                        FlatLabel().apply {
                            text = "Timeline"
                            putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxMuted")
                        },
                    )
                },
                "wrap",
            )
            add(timelineSlider, "growx, wrap")
            add(createTimelineControls(), "growx")
        }
    }

    private fun createTimelineControls(): JPanel {
        return JPanel(MigLayout("ins 0, gap 0, fillx", "[grow][pref!][grow]", "[][]")).apply {
            isOpaque = false
            add(createTransportControls(), "cell 1 0, center, wrap")
            add(createTimelineOptions(), "cell 1 1, center")
        }
    }

    private fun createTransportControls(): JPanel {
        return JPanel(MigLayout("ins 0, gap 6", "[][][][][][][]", "[]")).apply {
            isOpaque = false
            add(
                jumpStartButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    text = "Start"
                    toolTipText = "Seek to Start"
                    addActionListener {
                        seekToTick(0)
                    }
                },
                "w 42!, h 26!",
            )
            add(
                rewindButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    text = "-10"
                    toolTipText = "Rewind 10 Ticks"
                    addActionListener {
                        seekRelativeTicks(-SKIP_TICKS)
                    }
                },
                "w 38!, h 26!",
            )
            add(
                stepBackButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    icon = AppIcons.StepBack
                    toolTipText = "Previous Tick"
                    addActionListener {
                        replaySession?.stepBackTick()
                        refreshControls()
                    }
                },
                "w 28!, h 26!",
            )
            add(
                playButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    icon = AppIcons.Run
                    toolTipText = "Play"
                    addActionListener { togglePlayPause() }
                },
                "w 28!, h 26!",
            )
            add(
                stopButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    icon = AppIcons.Stop
                    toolTipText = "Stop"
                    addActionListener {
                        replaySession?.stop()
                        refreshControls()
                    }
                },
                "w 28!, h 26!",
            )
            add(
                stepForwardButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    icon = AppIcons.StepForward
                    toolTipText = "Next Tick"
                    addActionListener {
                        replaySession?.stepForwardTick()
                        refreshControls()
                    }
                },
                "w 28!, h 26!",
            )
            add(
                fastForwardButton.apply {
                    buttonType = FlatButton.ButtonType.toolBarButton
                    text = "+10"
                    toolTipText = "Fast Forward 10 Ticks"
                    addActionListener {
                        seekRelativeTicks(SKIP_TICKS)
                    }
                },
                "w 38!, h 26!",
            )
        }
    }

    private fun createTimelineOptions(): JPanel {
        return JPanel(MigLayout("ins 0, gap 10", "[][]20[]", "[]")).apply {
            isOpaque = false
            add(
                FlatLabel().apply {
                    text = "Tick"
                    putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxMuted")
                },
            )
            add(tickSpinner, "w 82!")
            add(createSpeedControls())
        }
    }

    private fun createSpeedControls(): JPanel {
        return JPanel(MigLayout("ins 0, gap 4", "[][][][][][][]", "[]")).apply {
            isOpaque = false
            add(
                FlatLabel().apply {
                    text = "Speed"
                    putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxMuted")
                },
            )
            listOf(0.25, 0.5, 1.0, 2.0, 4.0, 8.0).forEach { speed ->
                add(
                    FlatButton().apply {
                        text = formatSpeed(speed)
                        buttonType = FlatButton.ButtonType.toolBarButton
                        isFocusable = false
                        addActionListener {
                            selectedSpeed = speed
                            replaySession?.setSpeed(speed)
                            refreshControls()
                        }
                        speedButtons[speed] = this
                    },
                )
            }
        }
    }

    private fun createTranscriptControls(): JPanel {
        return JPanel(MigLayout("ins 0, gap 6, hidemode 3", "[][]", "[]")).apply {
            isOpaque = false
            add(
                followTranscriptButton.apply {
                    text = "Follow"
                    toolTipText = "Show transcript around the current replay tick"
                    putClientProperty("JButton.buttonType", "roundRect")
                    putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxFollow")
                    addActionListener {
                        renderTranscriptForCurrentState(force = true)
                        refreshControls()
                    }
                },
                "w 82!, h 28!",
            )
            add(
                retranscribeButton.apply {
                    text = "Retranscribe"
                    toolTipText = "Rebuild the transcript with current filters and settings"
                    addActionListener { retranscribeReplay() }
                },
                "w 112!, h 28!",
            )
        }
    }

    private fun retranscribeReplay() {
        val session = replaySession ?: return
        if (retranscribingReplay) {
            return
        }
        val generation = loadGeneration
        retranscribingReplay = true
        replayStatusText = "Transcribing replay"
        loadingProgress.isVisible = true
        statusLabel.text = replayStatusText
        refreshControls()
        ForkJoinPool.commonPool().submit {
            try {
                val transcript = App.service.transcribeReplaySession(session)
                SwingUtilities.invokeLater {
                    if (generation != loadGeneration || replaySession !== session) {
                        return@invokeLater
                    }
                    retranscribingReplay = false
                    loadingProgress.isVisible = false
                    replayTranscript = transcript
                    renderTranscriptForCurrentState(force = true)
                    replayStatusText = "Transcript updated"
                    statusLabel.text = replayStatusText
                    refreshControls()
                }
            } catch (t: Throwable) {
                SwingUtilities.invokeLater {
                    if (generation != loadGeneration || replaySession !== session) {
                        return@invokeLater
                    }
                    retranscribingReplay = false
                    loadingProgress.isVisible = false
                    refreshControls()
                    JOptionPane.showMessageDialog(
                        this,
                        t.message ?: "Unable to retranscribe replay.",
                        "Replay Transcribe Failed",
                        JOptionPane.ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun seekRelativeTicks(delta: Int) {
        val session = replaySession ?: return
        val snapshot = session.snapshot()
        seekToTick((snapshot.currentTick + delta).coerceIn(0, snapshot.totalTicks))
    }

    private fun seekToTick(tick: Int) {
        val session = replaySession ?: return
        session.seekToTick(tick)
        refreshControls()
    }

    private fun chooseReplayFile() {
        val chooser = createReplayFileChooser()
        if (chooser.showOpenDialog(this) != SystemFileChooser.APPROVE_OPTION) {
            return
        }
        val path = chooser.selectedFile?.toPath() ?: return
        openReplayFile(path)
    }

    private fun createReplayFileChooser(): SystemFileChooser {
        return SystemFileChooser().apply {
            stateStoreID = REPLAY_FILE_CHOOSER_STATE_ID
            fileSelectionMode = SystemFileChooser.FILES_ONLY
            if (!hasRememberedReplayChooserDirectory()) {
                setCurrentDirectory(defaultReplayDirectory().toFile())
                putPlatformProperty(SystemFileChooser.WINDOWS_DEFAULT_FOLDER, defaultReplayDirectory().toString())
            }
        }
    }

    private fun hasRememberedReplayChooserDirectory(): Boolean {
        val key = "$REPLAY_FILE_CHOOSER_STATE_ID.${SystemFileChooser.StateStore.KEY_CURRENT_DIRECTORY}"
        return SystemFileChooser.getStateStore()?.get(key, null) != null
    }

    private fun defaultReplayDirectory(): Path {
        val targetDirectory = BINARY_PATH.resolve(DEFAULT_REPLAY_BINARY_FOLDER)
        if (targetDirectory.exists()) {
            return targetDirectory
        }
        return BINARY_PATH
    }

    private fun launchReplayClient() {
        val session = replaySession ?: return
        val generation = loadGeneration
        val type = replayLaunchTypeModel.selectedItem as? SessionType ?: SessionType.RuneLite
        if (session.hasClientLaunchStarted()) {
            refreshControls()
            return
        }
        launchingReplayClient = true
        loadingProgress.isVisible = true
        replayStatusText = "Launching ${type.name}"
        statusLabel.text = replayStatusText
        refreshControls()
        ForkJoinPool.commonPool().submit {
            try {
                val port = 40000 + ProxyTarget.REPLAY_WORLD_ID
                when (type) {
                    SessionType.Native -> App.service.launchReplayNativeClient(session, port)
                    SessionType.RuneLite -> App.service.launchReplayRuneLiteClient(session, null, port)
                    SessionType.Java -> error("Unsupported replay client type: $type")
                }
                SwingUtilities.invokeLater {
                    if (generation != loadGeneration || replaySession !== session) {
                        return@invokeLater
                    }
                    launchingReplayClient = false
                    loadingProgress.isVisible = false
                    replayStatusText = "Client launched"
                    statusLabel.text = replayStatusText
                    refreshControls()
                }
            } catch (t: Throwable) {
                SwingUtilities.invokeLater {
                    if (generation != loadGeneration || replaySession !== session) {
                        return@invokeLater
                    }
                    launchingReplayClient = false
                    loadingProgress.isVisible = false
                    refreshControls()
                    JOptionPane.showMessageDialog(
                        this,
                        t.message ?: "Unable to launch replay client.",
                        "Replay Launch Failed",
                        JOptionPane.ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun togglePlayPause() {
        val session = replaySession ?: return
        when (session.snapshot().state) {
            ReplayPlaybackState.PLAYING,
            ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE,
            -> session.pause()
            else -> session.play()
        }
        refreshControls()
    }

    private fun updateEmptyState() {
        loadingReplay = false
        retranscribingReplay = false
        launchingReplayClient = false
        loadingProgress.isVisible = false
        titleLabel.text = "Replay"
        detailsLabel.text = "Open a binary dump to inspect and replay it."
        replayStatusText = ""
        statusLabel.text = replayStatusText
        statusLabel.toolTipText = null
        tickLabel.text = "Tick 0 / 0"
        frameLabel.text = "0 messages"
        selectedSpeed = 1.0
        timelineSlider.maximum = 0
        timelineSlider.value = 0
        tickSpinnerModel.maximum = 0
        tickSpinnerModel.value = 0
        replayTranscript = null
        transcriptDisplayMode = null
        liveTranscriptCenterTick = null
        followTranscriptButton.isSelected = false
        transcriptOffsetSpinnerModel.value = 0
    }

    private fun updateLoadingState(path: Path) {
        loadingReplay = true
        retranscribingReplay = false
        launchingReplayClient = false
        loadingProgress.isVisible = true
        titleLabel.text = path.name
        detailsLabel.text = "Loading replay dump..."
        replayStatusText = "Reading and transcribing"
        statusLabel.text = replayStatusText
        statusLabel.toolTipText = path.toString()
        tickLabel.text = "Tick 0 / 0"
        frameLabel.text = "Loading..."
        selectedSpeed = 1.0
        timelineSlider.maximum = 0
        timelineSlider.value = 0
        tickSpinnerModel.maximum = 0
        tickSpinnerModel.value = 0
        replayTranscript = null
        transcriptDisplayMode = null
        liveTranscriptCenterTick = null
    }

    private fun updateLoadedMetadata(transcript: ReplayTranscript) {
        loadingReplay = false
        launchingReplayClient = false
        loadingProgress.isVisible = false
        val session = replaySession ?: return
        val metadata = session.timeline.metadata
        val path = selectedPath
        val date = DateFormat.getDateTimeInstance().format(Date(metadata.timestamp))
        titleLabel.text = path?.name ?: "Replay loaded"
        detailsLabel.text =
            "Rev ${metadata.revision}.${metadata.subRevision} | World ${metadata.worldId} | " +
            "Player ${metadata.localPlayerIndex} | $date"
        replayStatusText = ""
        statusLabel.text = replayStatusText
        statusLabel.toolTipText = path?.toString() ?: metadata.clientName
        timelineSlider.maximum = session.timeline.totalTicks
        timelineSlider.value = 0
        tickSpinnerModel.maximum = session.timeline.totalTicks
        tickSpinnerModel.value = 0
        selectedSpeed = session.snapshot().speed
        replayTranscript = transcript
        renderTranscript(transcript, 0, force = true)
    }

    private fun renderTranscriptForCurrentState(force: Boolean = false) {
        val transcript = replayTranscript ?: return
        val snapshot = replaySession?.snapshot() ?: return
        renderTranscript(transcript, snapshot.currentTick, force)
    }

    private fun renderTranscript(
        transcript: ReplayTranscript,
        currentTick: Int,
        force: Boolean,
    ) {
        if (!followTranscriptButton.isSelected) {
            val centerTick = retainedTranscriptWindowTick(currentTick)
            if (force ||
                transcriptDisplayMode != TranscriptDisplayMode.WINDOW ||
                liveTranscriptCenterTick != centerTick
            ) {
                renderTranscriptWindow(transcript, centerTick)
            }
            syncReplayTreeToTick(currentTick)
            return
        }
        val centerTick = transcriptDisplayTick(currentTick)
        if (force || transcriptDisplayMode != TranscriptDisplayMode.LIVE || liveTranscriptCenterTick != centerTick) {
            renderLiveTranscript(transcript, centerTick)
        }
        syncReplayTreeToTick(centerTick)
    }

    private fun renderTranscriptWindow(
        transcript: ReplayTranscript,
        centerTick: Int,
    ) {
        clearReplayTree()
        val session = replaySession ?: return
        val streamNode = StreamTreeTableNode(session.timeline.header)
        addNodeAndExpand(streamNode, replayRoot, replayRoot.childCount)
        val startTick = (centerTick - TRANSCRIPT_WINDOW_RADIUS_TICKS).coerceAtLeast(0)
        val endTick = (centerTick + TRANSCRIPT_WINDOW_RADIUS_TICKS).coerceAtMost(session.timeline.totalTicks)
        val entries =
            transcript.entries
                .asSequence()
                .filter { it.tick in startTick..endTick }
                .groupBy { it.tick }
        val visibleMessageCount = renderTranscriptTicks(streamNode, entries, startTick, endTick)
        replayTree.expandAll()
        val messageCount = transcript.entries.size
        val visibleTickCount = endTick - startTick + 1
        frameLabel.text =
            "$visibleMessageCount of $messageCount messages | ticks $startTick-$endTick ($visibleTickCount ticks)"
        if (transcript.failures > 0) {
            frameLabel.text += " | ${transcript.failures} decode failures"
        }
        transcriptDisplayMode = TranscriptDisplayMode.WINDOW
        liveTranscriptCenterTick = centerTick
    }

    private fun renderTranscriptTicks(
        streamNode: AbstractMutableTreeTableNode,
        entries: Map<Int, List<ReplayTranscriptEntry>>,
        startTick: Int,
        endTick: Int,
    ): Int {
        var messageCount = 0
        for (tick in startTick..endTick) {
            val tickNode = TickTreeTableNode(tick)
            replayTickNodes[tick] = tickNode
            addNodeAndExpand(tickNode, streamNode, streamNode.childCount)
            val tickEntries = entries[tick].orEmpty()
            messageCount += tickEntries.size
            tickEntries.forEach { entry ->
                createMessageNode(tickNode, entry.node)
            }
        }
        return messageCount
    }

    private fun renderLiveTranscript(
        transcript: ReplayTranscript,
        centerTick: Int,
    ) {
        clearReplayTree()
        val session = replaySession ?: return
        val streamNode = StreamTreeTableNode(session.timeline.header)
        addNodeAndExpand(streamNode, replayRoot, replayRoot.childCount)
        val startTick = (centerTick - LIVE_TRANSCRIPT_RADIUS_TICKS).coerceAtLeast(0)
        val endTick = (centerTick + LIVE_TRANSCRIPT_RADIUS_TICKS).coerceAtMost(session.timeline.totalTicks)
        val entries =
            transcript.entries
                .asSequence()
                .filter { it.tick in startTick..endTick }
                .groupBy { it.tick }
        val visibleMessageCount = renderTranscriptTicks(streamNode, entries, startTick, endTick)
        replayTree.expandAll()
        val offsetSeconds = transcriptOffsetSeconds()
        val offsetText =
            when {
                offsetSeconds > 0 -> " +${offsetSeconds}s"
                offsetSeconds < 0 -> " ${offsetSeconds}s"
                else -> ""
            }
        val visibleTickCount = endTick - startTick + 1
        frameLabel.text =
            "Follow tick $centerTick$offsetText | $visibleMessageCount messages in $visibleTickCount ticks"
        if (transcript.failures > 0) {
            frameLabel.text += " | ${transcript.failures} decode failures"
        }
        transcriptDisplayMode = TranscriptDisplayMode.LIVE
        liveTranscriptCenterTick = centerTick
    }

    private fun transcriptDisplayTick(currentTick: Int): Int {
        val offsetTicks = secondsToTicks(transcriptOffsetSeconds())
        val totalTicks = replaySession?.timeline?.totalTicks ?: currentTick
        return (currentTick + offsetTicks).coerceIn(0, totalTicks)
    }

    private fun retainedTranscriptWindowTick(currentTick: Int): Int {
        val previousCenterTick = liveTranscriptCenterTick
        if (transcriptDisplayMode == TranscriptDisplayMode.WINDOW &&
            previousCenterTick != null &&
            currentTick in
            (previousCenterTick - TRANSCRIPT_WINDOW_RADIUS_TICKS)..(previousCenterTick + TRANSCRIPT_WINDOW_RADIUS_TICKS)
        ) {
            return previousCenterTick
        }
        return currentTick
    }

    private fun transcriptOffsetSeconds(): Int {
        return (transcriptOffsetSpinner.value as Number).toInt()
    }

    private fun secondsToTicks(seconds: Int): Int {
        return ((seconds * 1_000.0) / GAME_TICK_MILLIS).roundToInt()
    }

    private fun clearReplayTree() {
        replayTickNodes.clear()
        highlightedTick = null
        lastScrolledTick = null
        for (i in replayRoot.childCount - 1 downTo 0) {
            val child = replayRoot.getChildAt(i) as AbstractMutableTreeTableNode
            replayTreeModel.removeNodeFromParent(child)
        }
        frameLabel.text = "0 messages"
    }

    private fun createMessageNode(
        tickNode: AbstractMutableTreeTableNode,
        node: ReplayTranscriptNode,
    ) {
        val rootNode = MessageTreeTableNode(node.content.orEmpty(), node.protocol)
        addNodeAndExpand(rootNode, tickNode, tickNode.childCount)
        addChildNodes(rootNode, node)
    }

    private fun addChildNodes(
        parentNode: AbstractMutableTreeTableNode,
        node: ReplayTranscriptNode,
    ) {
        for (child in node.children) {
            val childNode = MessageTreeTableNode(child.content.orEmpty(), child.protocol)
            addNodeAndExpand(childNode, parentNode, parentNode.childCount)
            addChildNodes(childNode, child)
        }
    }

    private fun addNodeAndExpand(
        newChild: AbstractMutableTreeTableNode,
        parent: AbstractMutableTreeTableNode,
        index: Int,
    ) {
        replayTreeModel.insertNodeInto(newChild, parent, index)
        replayTree.expandRow(replayTree.rowCount - 1)
    }

    private fun getOddRowColor(): Color {
        val background = replayTree.background
        return if (FlatLaf.isLafDark()) {
            ColorFunctions.lighten(background, 0.05f)
        } else {
            ColorFunctions.darken(background, 0.05f)
        }
    }

    private fun getCurrentTickRowColor(): Color {
        val background = replayTree.background
        val accent =
            UIManager.getColor("Component.accentColor")
                ?: UIManager.getColor("Button.default.background")
                ?: replayTree.selectionBackground
                ?: background
        return mixColors(background, accent, if (FlatLaf.isLafDark()) 0.32f else 0.20f)
    }

    private fun isCurrentTickRow(row: Int): Boolean {
        val tick = highlightedTick ?: return false
        val tickNode = replayTickNodes[tick] ?: return false
        val path = replayTree.getPathForRow(row) ?: return false
        return path.path.any { it === tickNode }
    }

    private fun syncReplayTreeToTick(tick: Int) {
        if (highlightedTick != tick) {
            highlightedTick = tick
            replayTree.repaint()
        }
        if (lastScrolledTick == tick || replayTickNodes.isEmpty()) {
            return
        }
        val visibleTick =
            replayTickNodes.keys.firstOrNull { it >= tick }
                ?: replayTickNodes.keys.lastOrNull()
                ?: return
        val row = findRowForNode(replayTickNodes[visibleTick] ?: return) ?: return
        lastScrolledTick = tick
        val cellRect = replayTree.getCellRect(row, 0, true)
        val viewportHeight = replayTree.visibleRect.height.coerceAtLeast(cellRect.height)
        replayTree.scrollRectToVisible(Rectangle(0, cellRect.y, 1, viewportHeight))
    }

    private fun findRowForNode(node: AbstractMutableTreeTableNode): Int? {
        for (row in 0 until replayTree.rowCount) {
            val path = replayTree.getPathForRow(row) ?: continue
            if (path.lastPathComponent === node) {
                return row
            }
        }
        return null
    }

    private fun mixColors(
        base: Color,
        overlay: Color,
        amount: Float,
    ): Color {
        val boundedAmount = amount.coerceIn(0f, 1f)
        val inverse = 1f - boundedAmount
        return Color(
            (base.red * inverse + overlay.red * boundedAmount).toInt().coerceIn(0, 255),
            (base.green * inverse + overlay.green * boundedAmount).toInt().coerceIn(0, 255),
            (base.blue * inverse + overlay.blue * boundedAmount).toInt().coerceIn(0, 255),
        )
    }

    private fun refreshControls() {
        val session = replaySession
        val loaded = session != null && !loadingReplay
        val snapshot = session?.snapshot()
        val activeClient = session?.hasActiveClient() == true
        val waitingForMap = snapshot?.state == ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE
        val rebuilding = session?.isRebuildInProgress() == true || snapshot?.state == ReplayPlaybackState.SEEKING
        val transcriptBusy = retranscribingReplay
        val clientLaunchAvailable =
            loaded &&
                !launchingReplayClient &&
                !transcriptBusy &&
                !activeClient &&
                session?.hasClientLaunchStarted() != true
        val controlsEnabled = loaded && activeClient && !launchingReplayClient && !rebuilding
        val seekControlsEnabled = controlsEnabled && !waitingForMap
        loadingProgress.isVisible =
            loadingReplay ||
            launchingReplayClient ||
            rebuilding ||
            waitingForMap ||
            transcriptBusy
        openButton.isEnabled = !loadingReplay && !launchingReplayClient && !transcriptBusy
        launchButton.isEnabled = clientLaunchAvailable
        replayLaunchTypeDropdown.isEnabled = clientLaunchAvailable
        retranscribeButton.isEnabled = loaded && !transcriptBusy
        launchButton.text =
            when {
                launchingReplayClient -> "Launching"
                activeClient -> "Launched"
                session?.hasClientLaunchStarted() == true -> "Waiting"
                else -> "Launch"
            }
        timelineSlider.isEnabled = seekControlsEnabled
        jumpStartButton.isEnabled = seekControlsEnabled
        rewindButton.isEnabled = seekControlsEnabled
        stepBackButton.isEnabled = seekControlsEnabled
        playButton.isEnabled = controlsEnabled
        stopButton.isEnabled = controlsEnabled
        stepForwardButton.isEnabled = seekControlsEnabled
        fastForwardButton.isEnabled = seekControlsEnabled
        tickSpinner.isEnabled = seekControlsEnabled
        speedButtons.values.forEach { it.isEnabled = seekControlsEnabled }
        followTranscriptButton.isEnabled = loaded && replayTranscript != null
        transcriptOffsetSpinner.isEnabled = loaded && replayTranscript != null && followTranscriptButton.isSelected
        replayTree.isEnabled = loaded
        replayTree.tableHeader.isEnabled = loaded
        if (session == null) {
            playButton.icon = AppIcons.Run
            playButton.toolTipText = "Play"
            speedButtons.forEach { (speed, button) -> button.isSelected = speed == selectedSpeed }
            retranscribeButton.isEnabled = false
            return
        }
        val currentSnapshot = snapshot ?: session.snapshot()
        val displayTick = replayDisplayTick(session, currentSnapshot)
        if (!userAdjustingTimelineSlider && !timelineSlider.valueIsAdjusting) {
            updatingSlider = true
            timelineSlider.maximum = currentSnapshot.totalTicks
            timelineSlider.value = displayTick.coerceIn(0, currentSnapshot.totalTicks)
            updatingSlider = false
        }
        updatingTickSpinner = true
        tickSpinnerModel.maximum = currentSnapshot.totalTicks
        tickSpinnerModel.value = displayTick.coerceIn(0, currentSnapshot.totalTicks)
        updatingTickSpinner = false
        tickLabel.text =
            "Tick $displayTick / ${currentSnapshot.totalTicks}     Frame ${currentSnapshot.nextFrameIndex} / ${currentSnapshot.totalFrames}"
        replayTranscript?.let { transcript ->
            renderTranscript(transcript, displayTick, force = false)
        }
        selectedSpeed = currentSnapshot.speed
        speedButtons.forEach { (speed, button) -> button.isSelected = speed == selectedSpeed }
        val active =
            currentSnapshot.state == ReplayPlaybackState.PLAYING ||
                currentSnapshot.state == ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE
        playButton.icon = if (active) AppIcons.Pause else AppIcons.Run
        playButton.toolTipText = if (active) "Pause" else "Play"
        statusLabel.text =
            when {
                transcriptBusy -> "Transcribing replay"
                currentSnapshot.state == ReplayPlaybackState.SEEKING || rebuilding -> "Rebuilding replay state"
                currentSnapshot.state == ReplayPlaybackState.WAITING_FOR_MAP_BUILD_COMPLETE -> "Waiting for map load"
                currentSnapshot.state == ReplayPlaybackState.FINISHED -> "Finished"
                session.hasClientDisconnected() -> "Client disconnected"
                session.hasClientLaunchStarted() && !activeClient -> "Waiting for login"
                else -> replayStatusText
            }
    }

    private fun replayDisplayTick(
        session: ReplaySession,
        snapshot: ReplayPlaybackSnapshot,
    ): Int {
        val previousFrameIndex = snapshot.nextFrameIndex - 1
        return session.timeline.frames
            .getOrNull(previousFrameIndex)
            ?.tick
            ?: snapshot.currentTick
    }

    private fun formatSpeed(speed: Double): String {
        return if (speed % 1.0 == 0.0) {
            "${speed.toInt()}x"
        } else {
            "${speed}x"
        }
    }

    private companion object {
        private enum class TranscriptDisplayMode {
            WINDOW,
            LIVE,
        }

        private class StreamTreeTableNode(
            private val header: BinaryHeader,
        ) : ReplayTreeNode() {
            override fun getValueAt(column: Int): Any? =
                when (column) {
                    0 -> "Stream"
                    1 -> "World ${header.worldId} (${header.worldActivity}) at ${DateFormat.getTimeInstance().format(
                        Date(header.timestamp),
                    )}"
                    else -> error("Invalid column index: $column")
                }
        }

        private class TickTreeTableNode(
            private val tickNumber: Int,
        ) : ReplayTreeNode() {
            override fun getValueAt(column: Int): Any? =
                when (column) {
                    0 -> "Tick $tickNumber"
                    1 -> null
                    else -> error("Invalid column index: $column")
                }
        }

        private class MessageTreeTableNode(
            private val message: String,
            private val prot: Any?,
        ) : ReplayTreeNode() {
            override fun getValueAt(column: Int): Any? =
                when (column) {
                    0 -> prot?.toString()?.lowercase()
                    1 -> message
                    else -> error("Invalid column index: $column")
                }
        }

        private abstract class ReplayTreeNode : AbstractMutableTreeTableNode(null, true) {
            override fun getColumnCount(): Int = 2
        }

        private const val TRANSCRIPT_WINDOW_RADIUS_TICKS = 250
        private const val LIVE_TRANSCRIPT_RADIUS_TICKS = 2
        private const val GAME_TICK_MILLIS = 600
        private const val DEFAULT_REPLAY_BINARY_FOLDER = "Old School RuneScape"
        private const val REPLAY_FILE_CHOOSER_STATE_ID = "replayFile"
        private const val SKIP_TICKS = 10
    }

    private inner class CurrentTickHighlightPredicate : HighlightPredicate {
        override fun isHighlighted(
            renderer: Component,
            adapter: ComponentAdapter,
        ): Boolean {
            return isCurrentTickRow(adapter.row)
        }
    }
}
