package net.rsprox.gui.dialogs

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatSeparator
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.AppIcons
import java.awt.Cursor
import java.awt.Desktop
import java.awt.event.KeyEvent
import java.net.URI
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.KeyStroke

internal class SponsorshipDialog(
    parent: JFrame,
) : JDialog(parent, "RSProx — Sponsorship", true) {
    init {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isResizable = false
        contentPane = createContent()
        rootPane.registerKeyboardAction(
            { dispose() },
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW,
        )
        pack()
        setLocationRelativeTo(parent)
    }

    private fun createContent(): JPanel {
        return JPanel(MigLayout("ins 0, gap 0, wrap 1", "[560!, fill]", "[]0[]0[]")).apply {
            add(
                JPanel(MigLayout("ins 28, gapy 16, wrap 1", "[fill, grow]", "[]20[][][]")).apply {
                    add(
                        FlatLabel().apply {
                            text = "Keep RSProx open to everyone"
                            icon = AppIcons.Heart.derive(24, 24)
                            iconTextGap = 12
                            font = font.deriveFont(21f)
                        },
                    )
                    add(
                        paragraph(
                            "RSProx is free to use. Sponsorship helps keep it that way by funding " +
                                "ongoing maintenance, compatibility updates, and new features.",
                        ),
                    )
                    add(
                        paragraph(
                            "Voluntary support is the preferred path forward: people who find value in " +
                                "RSProx help keep it available to everyone. If that model proves unsustainable, " +
                                "continued development will need to be funded through paid access.",
                        ),
                    )
                    add(paragraph("If RSProx is part of your toolkit, consider becoming a sponsor."))
                },
            )
            add(FlatSeparator(), "growx")
            add(
                JPanel(MigLayout("ins 18 28 18 28, gap 12, fillx", "[]push[]", "[]")).apply {
                    add(
                        FlatButton().apply {
                            text = "Sponsor on GitHub"
                            icon = AppIcons.Heart
                            iconTextGap = 8
                            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            isRequestFocusEnabled = false
                            putClientProperty(
                                FlatClientProperties.STYLE_CLASS,
                                "rsproxSponsor rsproxSponsorDialog",
                            )
                            addActionListener { openSponsors() }
                        },
                        "h 34!",
                    )
                    add(
                        FlatButton().apply {
                            text = "Continue to RSProx"
                            addActionListener { dispose() }
                        },
                        "h 34!",
                    )
                },
            )
        }
    }

    private fun paragraph(text: String): FlatLabel {
        return FlatLabel().apply {
            font = font.deriveFont(13f)
            this.text = "<html><body style='width: 375px'>$text</body></html>"
        }
    }

    private fun openSponsors() {
        runCatching {
            check(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                "Opening a browser is not supported on this system."
            }
            Desktop.getDesktop().browse(URI("https://github.com/sponsors/blurite"))
        }.onSuccess {
            dispose()
        }.onFailure {
            Dialogs.showError(
                this,
                "Unable to open GitHub Sponsors",
                "Please visit https://github.com/sponsors/blurite in your browser.",
            )
        }
    }
}
