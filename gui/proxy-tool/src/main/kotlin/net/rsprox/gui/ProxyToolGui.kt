package net.rsprox.gui

import com.formdev.flatlaf.FlatDarkLaf
import net.rsprox.gui.components.ClientPanel
import net.rsprox.gui.components.createClientsPanel
import java.awt.BorderLayout
import java.util.Locale
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTabbedPane

public class ProxyToolGui : JFrame() {
    init {
        title = "RSProx"
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(320, 230)
        isResizable = false
        setLocationRelativeTo(null)
        layout = BorderLayout()
        val panel = JPanel()
        panel.layout = BorderLayout()
        add(panel, BorderLayout.CENTER)
        createTabs(panel)
        isVisible = true
    }

    private lateinit var clientsPanel: ClientPanel

    private fun createTabs(panel: JPanel) {
        val tabPane = JTabbedPane()
        tabPane.tabPlacement = JTabbedPane.TOP
        tabPane.tabLayoutPolicy = JTabbedPane.SCROLL_TAB_LAYOUT

        val clientsPane = JPanel()
        clientsPane.layout = BorderLayout()
        this.clientsPanel = createClientsPanel(clientsPane)

        tabPane.addTab("Clients", clientsPane)
        // tabPane.addTab("Sessions", sessionsPane)
        // tabPane.addTab("Settings", settingsPane)
        // tabPane.addTab("About", aboutPane)
        panel.add(tabPane)
    }
}

public fun main() {
    Locale.setDefault(Locale.US)
    FlatDarkLaf.setup()
    ProxyToolGui()
}
