package net.rsprox.gui

import com.formdev.flatlaf.extras.FlatSVGUtils
import com.formdev.flatlaf.extras.components.FlatMenuBar
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*
import com.formdev.flatlaf.util.UIScale
import io.netty.buffer.UnpooledByteBufAllocator
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.dialogs.AboutDialog
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.gui.sidebar.SideBar
import net.rsprox.proxy.ProxyService
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE
import kotlin.system.exitProcess

public class App {
    private val frame = JFrame()
    private val sessionsPanel = SessionsPanel(this)
    public val statusBar: StatusBar = StatusBar()
    public lateinit var service: ProxyService

    public fun init() {
        // We have to start before populating right now.
        service = ProxyService(UnpooledByteBufAllocator.DEFAULT)
        service.start()

        val defaultSize = UIScale.scale(Dimension(800, 600))

        // Configure the app frame.
        frame.title = "RSProx v${service.getAppVersion()}"
        frame.defaultCloseOperation = DO_NOTHING_ON_CLOSE
        frame.size = defaultSize
        frame.minimumSize = defaultSize
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

        // Configure theme
        AppThemes.applyTheme(service.getAppTheme()) {
            SwingUtilities.updateComponentTreeUI(frame)
        }
    }

    public fun start() {
        frame.isVisible = true
        frame.setLocationRelativeTo(null)
    }

    private fun setupMenuBar() {
        val menuBar = FlatMenuBar()
        menuBar.createThemes()
        menuBar.createHelpItems()
        frame.jMenuBar = menuBar
    }

    private fun FlatMenuBar.createThemes() {
        val menu = JMenu("Themes")

        AppThemes.THEMES.forEach {
            val name = it.first
            menu.add(
                JMenuItem(name).apply {
                    addActionListener {
                        AppThemes.applyTheme(name) {
                            SwingUtilities.updateComponentTreeUI(frame)
                            service.setAppTheme(name)
                        }
                    }
                }
            )
        }

        add(menu)
    }

    private fun FlatMenuBar.createHelpItems() {
        add(
            JMenu("Help").apply {
                val aboutItem = JMenuItem("About")
                aboutItem.accelerator = KeyStroke.getKeyStroke("F1")
                aboutItem.mnemonic = 'A'.code
                aboutItem.addActionListener {
                    val dialog = AboutDialog(frame, service.getAppVersion())
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
