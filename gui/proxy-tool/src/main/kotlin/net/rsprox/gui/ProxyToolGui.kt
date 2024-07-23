package net.rsprox.gui

import com.formdev.flatlaf.FlatDarkLaf
import com.github.michaelbull.logging.InlineLogger
import io.netty.buffer.UnpooledByteBufAllocator
import net.rsprox.gui.components.ClientPanel
import net.rsprox.gui.components.createClientsPanel
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.progressbar.ProgressBarNotifier
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.*
import java.util.concurrent.ForkJoinPool
import javax.swing.JFrame
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import kotlin.system.exitProcess
import kotlin.time.measureTime

public class ProxyToolGui : JFrame() {
    private lateinit var clientsPanel: ClientPanel
    private val service = ProxyService(UnpooledByteBufAllocator.DEFAULT)

    init {
        title = "RSProx"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        setSize(320, 230)
        isResizable = false
        setLocationRelativeTo(null)
        layout = BorderLayout()
        val panel = JPanel()
        panel.layout = BorderLayout()
        add(panel, BorderLayout.CENTER)
        createTabs(panel)
        isVisible = true
        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    var result = JOptionPane.OK_OPTION

                    if (service.hasAliveProcesses()) {
                        try {
                            result =
                                JOptionPane.showConfirmDialog(
                                    this@ProxyToolGui,
                                    "Are you sure you want to exit?",
                                    "Exit",
                                    JOptionPane.OK_CANCEL_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                )
                        } catch (e: Exception) {
                            logger.warn(e) {
                                "Unexpected exception occurred while check for confirm required"
                            }
                        }
                    }

                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            service.killAliveProcesses()
                        } finally {
                            service.safeShutdown()
                        }
                        exitProcess(0)
                    }
                }
            },
        )
    }

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

        registerNativeButton()
        service.start()
    }

    private fun registerNativeButton() {
        clientsPanel.native.addActionListener {
            val logger = InlineLogger("ProxyService")
            ForkJoinPool.commonPool().submit {
                SwingUtilities.invokeAndWait {
                    this.clientsPanel.native.isEnabled = false
                    this.clientsPanel.runelite.isEnabled = false
                    this.clientsPanel.custom.isEnabled = false
                }
                val time =
                    measureTime {
                        val progressBar =
                            ProgressBarNotifier { percentage, text ->
                                SwingUtilities.invokeLater {
                                    this.clientsPanel.progressBar.isVisible = true
                                    this.clientsPanel.progressBar.value = percentage
                                    this.clientsPanel.progressBar.string = text
                                }
                            }
                        service.launchNativeClient(progressBar)
                        SwingUtilities.invokeLater {
                            this.clientsPanel.progressBar.isVisible = false
                        }
                    }
                logger.info { "Native client started in $time" }
                SwingUtilities.invokeAndWait {
                    this.clientsPanel.native.isEnabled = true
                    this.clientsPanel.runelite.isEnabled = true
                    this.clientsPanel.custom.isEnabled = true
                }
            }
        }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}

public fun main() {
    Locale.setDefault(Locale.US)
    FlatDarkLaf.setup()
    ProxyToolGui()
}
