package net.rsprox.gui

import com.formdev.flatlaf.extras.FlatSVGUtils
import com.formdev.flatlaf.extras.components.FlatMenuBar
import com.formdev.flatlaf.util.UIScale
import io.netty.buffer.UnpooledByteBufAllocator
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.dialogs.AboutDialog
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.gui.sidebar.SideBar
import net.rsprox.proxy.ProxyService
import java.awt.Dimension
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
import kotlin.system.exitProcess

public class App {

    public var service: ProxyService = ProxyService(UnpooledByteBufAllocator.DEFAULT)

    private val frame: JFrame
    private val sessionsPanel: SessionsPanel
    public val statusBar: StatusBar

    init {
        // Start the service right away so we can load the properties for app theme
        // and presets data.
        service.start()

        // Setup the theme before any components are created.
        AppThemes.applyThemeEdt(service.getAppTheme())

        // Create the main components.
        frame = JFrame()
        sessionsPanel = SessionsPanel(this)
        statusBar = StatusBar()
    }

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

        // Configure the app frame.
        frame.title = "RSProx v${AppProperties.version}"
        frame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        frame.size = defaultSize
        frame.minimumSize = UIScale.scale(Dimension(800, 600))
        frame.iconImages = FlatSVGUtils.createWindowIconImages("/favicon.svg")
        frame.addWindowListener(
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
            },
        )

        frame.addComponentListener(
            object : ComponentAdapter() {
                override fun componentResized(e: ComponentEvent) {
                    service.setAppSize(e.component.width, e.component.height)
                }

                override fun componentMoved(e: ComponentEvent) {
                    service.setAppPosition(e.component.x, e.component.y)
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
        menuBar.createThemes()
        menuBar.createHelpItems()
        frame.jMenuBar = menuBar
    }

    private fun FlatMenuBar.createThemes() {
        val menu = JMenu("Themes")
        menu.mnemonic = 'T'.code

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

    private fun createSideBar() =
        SideBar().apply {
//        addButton(AppIcons.Settings, "Sessions", JPanel())
            addButton(AppIcons.Filter, "Filters", FiltersSidePanel(service))
            selectedIndex = -1
        }

    private fun createSessionsPanelContainer() =
        JPanel().apply {
            layout = MigLayout("ins 0, gap 0", "[fill, grow]", "[fill, grow]")
            add(sessionsPanel)
        }
}
