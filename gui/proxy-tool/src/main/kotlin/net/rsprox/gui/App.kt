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
import javax.swing.*

public class App {

    private val frame = JFrame()
    private val sessionsPanel = SessionsPanel(this)
    public val statusBar: StatusBar = StatusBar()
    public val service: ProxyService = ProxyService(UnpooledByteBufAllocator.DEFAULT)

    public fun init() {
        val defaultSize = UIScale.scale(Dimension(800, 600))

        // Configure the app frame.
        frame.title = "RSProx v${AppProperties.version}"
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.size = defaultSize
        frame.minimumSize = defaultSize
        frame.iconImages = FlatSVGUtils.createWindowIconImages("/favicon.svg")

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
        frame.setLocationRelativeTo(null)
        service.start()
    }

    private fun setupMenuBar() {
        val menuBar = FlatMenuBar()
        menuBar.add(JMenu("RSProx"))
        menuBar.add(JMenu("Help").apply {
            val aboutItem = JMenuItem("About")
            aboutItem.accelerator = KeyStroke.getKeyStroke("F1")
            aboutItem.mnemonic = 'A'.code
            aboutItem.addActionListener {
                val dialog = AboutDialog(frame)
                dialog.isVisible = true
            }
            add(aboutItem)
        })
        frame.jMenuBar = menuBar
    }

    private fun createSideBar() = SideBar().apply {
        addButton(AppIcons.Settings, "Sessions", JPanel())
        addButton(AppIcons.Filter, "Filters", JPanel())
        selectedIndex = -1
    }

    private fun createSessionsPanelContainer() = JPanel().apply {
        layout = MigLayout("ins 0, gap 0", "[fill, grow]", "[fill, grow]")
        add(sessionsPanel)
    }
}
