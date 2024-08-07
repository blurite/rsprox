package net.rsprox.gui.dialogs

import com.formdev.flatlaf.extras.FlatSVGUtils
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatLabel
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.AppProperties
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.net.URI
import javax.swing.ImageIcon
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

public class AboutDialog(
    parent: JFrame,
) : JDialog() {
    init {
        title = "About RSProx"
        setSize(400, 320)
        setLocationRelativeTo(parent)
        isResizable = false
        isModal = true
        modalityType = ModalityType.APPLICATION_MODAL
        iconImages = FlatSVGUtils.createWindowIconImages("/favicon.svg")

        val panel = JPanel(MigLayout("insets 30 30 n n, wrap 2", "[left]20[grow,fill]", "[][][]15[]15[][]push[]"))

        panel.add(JLabel(ImageIcon(FlatSVGUtils.svg2image("/favicon.svg", 48, 48))))
        panel.add(
            FlatLabel().apply {
                text = "RSProx v${AppProperties.version}"
                labelType = FlatLabel.LabelType.h1
            },
        )

        panel.add(
            FlatLabel().apply {
                text = "RSProx is a free and open-source project."
                labelType = FlatLabel.LabelType.regular
            },
            "skip 1, wrap",
        )
        panel.add(
            FlatLabel().apply {
                text = "It is licensed under the MIT License."
                labelType = FlatLabel.LabelType.regular
            },
            "skip 1, wrap",
        )

        panel.add(
            FlatLabel().apply {
                text = "Java Version: ${System.getProperty("java.version")}"
                labelType = FlatLabel.LabelType.regular
            },
            "skip 1, wrap",
        )

        panel.add(createLinkLabel("GitHub Repository", "https://github.com/blurite/rsprox"), "skip 1, wrap")
        panel.add(createLinkLabel("Join us at Discord", "https://discord.gg/blurite"), "skip 1, wrap")

        panel.add(
            FlatButton().apply {
                text = "Close"
                addActionListener { dispose() }
            },
            "skip 1, align right",
        )

        add(panel)
    }

    private fun createLinkLabel(
        text: String,
        url: String,
    ): FlatLabel {
        val label = FlatLabel()
        label.text = "<html><a href=\"$url\">$text</a></html>"
        label.labelType = FlatLabel.LabelType.regular
        label.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        label.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(URI(url))
                    }
                }
            },
        )
        return label
    }
}
