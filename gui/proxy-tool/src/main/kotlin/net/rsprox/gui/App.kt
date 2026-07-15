package net.rsprox.gui

import com.formdev.flatlaf.extras.FlatSVGUtils
import com.formdev.flatlaf.extras.components.FlatMenuBar
import com.formdev.flatlaf.util.SystemFileChooser
import com.formdev.flatlaf.util.UIScale
import io.netty.buffer.ByteBufAllocator
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.components.LaunchBar
import net.rsprox.gui.dialogs.AboutDialog
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.gui.sidebar.SideBar
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.config.BINARY_PATH
import net.rsprox.proxy.config.ERROR_LOGS_PATH
import java.awt.CardLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.URI
import java.util.prefs.Preferences
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
import kotlin.system.exitProcess

public class App {
    private val frame: JFrame = JFrame()
    private val sessionsPanel: SessionsPanel = SessionsPanel(this)
    private val launchBar: LaunchBar = LaunchBar(sessionsPanel)
    private val sessionWorkspaceLayout: CardLayout = CardLayout()
    private val sessionWorkspacePanel: JPanel = JPanel(sessionWorkspaceLayout)
    private val replayPanel: ReplayPanel = ReplayPanel { path -> recordRecentReplayDump(path) }
    private val transcriptionManager: TranscriptionManager = TranscriptionManager()
    public val statusBar: StatusBar = StatusBar(transcriptionManager)
    private lateinit var sideBar: SideBar
    private lateinit var homePanel: HomePanel

    public fun init() {
        installFileChooserStateStore()
        val width = service.getAppWidth()
        val height = service.getAppHeight()
        val defaultSize = UIScale.scale(Dimension(width, height))
        val posX = service.getAppPositionX()
        val posY = service.getAppPositionY()
        if (posX != null && posY != null) {
            frame.setLocation(posX, posY)
        } else {
            frame.setLocationRelativeTo(null)
        }

        val maximized = service.getAppMaximized()
        if (maximized != null) {
            frame.extendedState = if (maximized) JFrame.MAXIMIZED_BOTH else JFrame.NORMAL
        }

        // Configure the app frame.
        frame.title = "RSProx v${AppProperties.version}"
        frame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        frame.size = defaultSize
        frame.minimumSize = UIScale.scale(Dimension(800, 600))
        frame.iconImages = FlatSVGUtils.createWindowIconImages("/favicon.svg")
        if (!validFramePosition(frame)) {
            frame.setLocationRelativeTo(null)
        }
        val windowHandler =
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    val confirmed =
                        sessionsPanel.sessionCount == 0 ||
                            JOptionPane.showConfirmDialog(
                                frame,
                                "Are you sure you want to exit?",
                                "Exit",
                                JOptionPane.YES_NO_OPTION,
                            ) == JOptionPane.YES_OPTION
                    if (confirmed) {
                        try {
                            service.safeShutdown()
                        } finally {
                            exitProcess(0)
                        }
                    }
                }

                override fun windowStateChanged(e: WindowEvent) {
                    service.setAppMaximized(e.newState == JFrame.MAXIMIZED_BOTH)
                }
            }

        frame.addWindowListener(windowHandler)
        frame.addWindowStateListener(windowHandler)

        frame.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    if (frame.extendedState == JFrame.NORMAL) {
                        service.setAppSize(e.component.width, e.component.height)
                    }
                }

                override fun componentMoved(e: ComponentEvent) {
                    if (frame.extendedState == JFrame.NORMAL) {
                        service.setAppPosition(e.component.x, e.component.y)
                    }
                }
            },
        )

        // Configure the app content.
        val content = JPanel()
        content.layout = MigLayout("gap 0, ins 0, hidemode 3", "[fill, grow][]", "[fill, grow][fill]")
        content.border = BorderFactory.createEmptyBorder()
        content.add(createSessionsPanelContainer())
        content.add(createSideBar(), "wrap")
        content.add(statusBar, "spanx 2")
        frame.contentPane = content

        // Configure the app menu bar.
        setupMenuBar()

        frame.transferHandler =
            FileDropHandler(
                service,
                transcriptionManager,
                openReplay = { openReplayDump(it) },
            )
    }

    public fun start() {
        frame.isVisible = true
    }

    private fun setupMenuBar() {
        val menuBar = FlatMenuBar()
        menuBar.createFileMenu()
        menuBar.createThemes()
        menuBar.createHelpItems()
        frame.jMenuBar = menuBar
    }

    private fun FlatMenuBar.createFileMenu() {
        add(
            JMenu("File").apply {
                mnemonic = 'F'.code

                val openBinaryLogsFolder = JMenuItem("Open Binary Logs Folder")
                openBinaryLogsFolder.mnemonic = 'O'.code
                openBinaryLogsFolder.addActionListener {
                    this@App.openBinaryLogsFolder()
                }
                add(openBinaryLogsFolder)

                val openErrorLogsFolder = JMenuItem("Open Error Logs Folder")
                openErrorLogsFolder.mnemonic = '1'.code
                openErrorLogsFolder.addActionListener {
                    this@App.openErrorLogsFolder()
                }
                add(openErrorLogsFolder)

                addSeparator()

                val exitItem = JMenuItem("Exit")
                exitItem.mnemonic = 'X'.code
                exitItem.accelerator = KeyStroke.getKeyStroke("alt F4")
                exitItem.addActionListener {
                    frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
                }

                add(exitItem)
            },
        )
    }

    private fun FlatMenuBar.createThemes() {
        val menu = JMenu("Themes")
        menu.mnemonic = 'T'.code

        menu.add(
            JMenuItem("RuneLite (default)").apply {
                addActionListener {
                    service.setAppTheme("RuneLite")
                    AppThemes.applyTheme("RuneLite")
                }
            },
        )

        AppThemes.THEMES.forEach {
            val name = it.name
            menu.add(
                JMenuItem(name).apply {
                    addActionListener {
                        service.setAppTheme(name)
                        AppThemes.applyTheme(name)
                    }
                },
            )
        }

        add(menu)
    }

    private fun FlatMenuBar.createHelpItems() {
        add(
            JMenu("Help").apply {
                mnemonic = 'H'.code
                val aboutItem = JMenuItem("About")
                aboutItem.accelerator = KeyStroke.getKeyStroke("F1")
                aboutItem.mnemonic = 'A'.code
                aboutItem.addActionListener {
                    val dialog = AboutDialog(frame)
                    dialog.isVisible = true
                }
                add(aboutItem)
            },
        )
    }

    private fun createSideBar(): SideBar {
        sideBar =
            SideBar().apply {
                addButton(AppIcons.Settings, "Settings", SettingsSidePanel(service))
                addButton(AppIcons.Filter, "Filters", FiltersSidePanel(service))
                @Suppress("LiftReturnOrAssignment")
                try {
                    selectedIndex = service.getFiltersStatus()
                } catch (_: IndexOutOfBoundsException) {
                    // Fall back to the first tab; this can only happen in development of a new tab
                    // on another branch (and switching to an older branch). Avoids crashing the app
                    // entirely and allows normal usage without needing to edit the config files.
                    selectedIndex = 0
                }
            }
        return sideBar
    }

    private fun createSessionsPanelContainer() =
        JPanel().apply {
            layout = MigLayout("ins 0, gap 0", "[fill, grow]", "[fill, grow]")
            add(createSessionWorkspacePanel(), "grow, push")
        }

    private fun createSessionWorkspacePanel(): JPanel {
        homePanel =
            HomePanel(
                launchBar = launchBar,
                onReplay = { openReplayPanel() },
                onReplayDump = { openReplayDump(it) },
                onOpenLogs = { openBinaryLogsFolder() },
                onReportBug = { openUrl(BUG_REPORT_URI) },
            )
        sessionsPanel.installHomePanel(homePanel)
        sessionsPanel.installReplayPanel(replayPanel)
        sessionWorkspacePanel.add(sessionsPanel, SESSIONS_VIEW)
        refreshSessionWorkspace()
        return sessionWorkspacePanel
    }

    public fun refreshSessionWorkspace() {
        sessionWorkspaceLayout.show(sessionWorkspacePanel, SESSIONS_VIEW)
    }

    private fun openReplayPanel() {
        sessionsPanel.showReplayPanel()
    }

    private fun openReplayDump(path: java.nio.file.Path) {
        openReplayPanel()
        replayPanel.openReplayFile(path)
    }

    private fun recordRecentReplayDump(path: java.nio.file.Path) {
        RecentReplayDumps.record(path)
        homePanel.refreshRecentDumps()
    }

    private fun openBinaryLogsFolder() {
        openFolder(BINARY_PATH, "You have not created any logs yet.")
    }

    private fun openErrorLogsFolder() {
        openFolder(ERROR_LOGS_PATH, "You have not had any error logs yet.")
    }

    private fun openFolder(
        path: java.nio.file.Path,
        missingMessage: String,
    ) {
        val file = path.toFile()
        if (file.exists()) {
            Desktop.getDesktop().open(file)
        } else {
            JOptionPane.showMessageDialog(
                frame,
                missingMessage,
                "Error",
                JOptionPane.ERROR_MESSAGE,
            )
        }
    }

    private fun openUrl(uri: URI) {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri)
        }
    }

    /**
     * Checks if the frame intersects with any screen device. If the function returns false, the window
     * is completely outside the device's visible area, meaning the user would not be able to interact
     * with the window.
     */
    private fun validFramePosition(frame: JFrame): Boolean {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices.any { device ->
            device.defaultConfiguration.bounds.intersects(frame.bounds)
        }
    }

    private fun installFileChooserStateStore() {
        if (SystemFileChooser.getStateStore() != null) {
            return
        }
        val preferences = Preferences.userNodeForPackage(App::class.java).node("fileChooser")
        SystemFileChooser.setStateStore(
            object : SystemFileChooser.StateStore {
                override fun get(
                    key: String,
                    def: String?,
                ): String? = preferences.get(key, def)

                override fun put(
                    key: String,
                    value: String?,
                ) {
                    if (value == null) {
                        preferences.remove(key)
                    } else {
                        preferences.put(key, value)
                    }
                }
            },
        )
    }

    public companion object {
        public val service: ProxyService = ProxyService(ByteBufAllocator.DEFAULT)
        private val BUG_REPORT_URI: URI = URI("https://github.com/blurite/rsprox/issues/new")
        private const val SESSIONS_VIEW: String = "sessions"
    }
}
