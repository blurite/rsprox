package net.rsprox.gui

import com.formdev.flatlaf.extras.FlatSVGUtils
import com.formdev.flatlaf.extras.components.FlatMenuBar
import com.formdev.flatlaf.util.UIScale
import io.netty.buffer.ByteBufAllocator
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.dialogs.AboutDialog
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.gui.sidebar.SideBar
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.config.BINARY_PATH
import java.awt.Desktop
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
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
    public val statusBar: StatusBar = StatusBar()

    public fun init() {
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
        val windowHandler =
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    val confirmed =
                        sessionsPanel.tabCount < 1 ||
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

                val openLogsFolder = JMenuItem("Open Binary Logs Folder")
                openLogsFolder.mnemonic = 'O'.code
                openLogsFolder.addActionListener {
                    val path = BINARY_PATH.toFile()
                    if (path.exists()) {
                        Desktop.getDesktop().open(path)
                    } else {
                        JOptionPane.showMessageDialog(
                            frame,
                            "You have not created any logs yet.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE,
                        )
                    }
                }
                add(openLogsFolder)

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
        return SideBar().apply {
            //        addButton(AppIcons.Settings, "Sessions", JPanel())
            addButton(AppIcons.Filter, "Filters", FiltersSidePanel(service))
            selectedIndex = service.getFiltersStatus()
        }
    }

    private fun createSessionsPanelContainer() =
        JPanel().apply {
            layout = MigLayout("ins 0, gap 0", "[fill, grow]", "[fill, grow]")
            add(sessionsPanel)
        }

    public companion object {
        public val service: ProxyService = ProxyService(ByteBufAllocator.DEFAULT)
    }
}
