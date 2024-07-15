package net.rsprox.gui.components

import net.rsprox.gui.ProxyToolGui
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingConstants

internal data class ClientPanel(
    val progressBar: JProgressBar,
    val native: JButton,
    val runelite: JButton,
    val custom: JButton,
)

internal fun createClientsPanel(parent: JPanel): ClientPanel {
    parent.layout = BorderLayout()
    parent.minimumSize = Dimension(300, 160)
    parent.preferredSize = Dimension(300, 160)
    parent.maximumSize = Dimension(300, 160)
    val (native, runelite, custom) = listClients(parent)
    val bottomPanel = JPanel()
    val progressBar = createProgressBar(bottomPanel)
    parent.add(bottomPanel, BorderLayout.SOUTH)
    return ClientPanel(progressBar, native, runelite, custom)
}

private fun listClients(parent: JPanel): List<JButton> {
    val clientsPanel = JPanel()
    val nativeIcon = getImageIcon("native.png")
    val runeliteIcon = getImageIcon("runelite.png")
    val javaIcon = getImageIcon("java.png")

    val native = JButton("Native", nativeIcon)
    native.toolTipText = "Native"
    native.horizontalTextPosition = SwingConstants.CENTER
    native.verticalTextPosition = SwingConstants.BOTTOM
    clientsPanel.add(native)

    val runelite = JButton(runeliteIcon)
    runelite.toolTipText = "RuneLite"
    runelite.text = "RuneLite"
    runelite.horizontalTextPosition = SwingConstants.CENTER
    runelite.verticalTextPosition = SwingConstants.BOTTOM
    clientsPanel.add(runelite)

    val custom = JButton(javaIcon)
    custom.text = "Custom"
    custom.toolTipText = "Custom (Java clients only - no launchers)"
    custom.horizontalTextPosition = SwingConstants.CENTER
    custom.verticalTextPosition = SwingConstants.BOTTOM
    clientsPanel.add(custom)

    parent.add(clientsPanel, BorderLayout.CENTER)
    return listOf(native, runelite, custom)
}

private fun createProgressBar(parent: JPanel): JProgressBar {
    val progressBar = JProgressBar()
    progressBar.isStringPainted = true
    progressBar.isVisible = false
    parent.add(progressBar)
    return progressBar
}

private fun getImageIcon(name: String): ImageIcon {
    val imgURL =
        ProxyToolGui::class.java.getResource(name)
            ?: throw IllegalStateException("Resource $name not found.")
    return ImageIcon(imgURL)
}
