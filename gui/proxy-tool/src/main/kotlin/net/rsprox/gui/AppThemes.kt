package net.rsprox.gui

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.IntelliJTheme
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes
import com.github.michaelbull.logging.InlineLogger
import java.awt.EventQueue
import javax.swing.UIManager
import javax.swing.UIManager.LookAndFeelInfo

public object AppThemes {
    private val log = InlineLogger()

    public val THEMES: List<LookAndFeelInfo> =
        buildList {
            for (flatLafInfo in FlatAllIJThemes.INFOS) {
                if (flatLafInfo.isDark) {
                    add(flatLafInfo)
                }
            }
        }

    public fun applyTheme(name: String) {
        EventQueue.invokeLater {
            applyThemeEdt(name)
        }
    }

    public fun applyThemeEdt(name: String) {
        check(EventQueue.isDispatchThread()) { "Must be called on the EDT" }
        try {
            val isCurrentLaf = UIManager.getLookAndFeel() is FlatLaf
            val lafClass = THEMES.firstOrNull { it.name == name }?.className
            if (isCurrentLaf) {
                FlatAnimatedLafChange.showSnapshot()
            }
            if (lafClass != null) {
                UIManager.setLookAndFeel(lafClass)
            } else {
                // The L&F must be re-loaded every time or artefacting will occur
                UIManager.setLookAndFeel(
                    IntelliJTheme.createLaf(
                        IntelliJTheme(
                            App::class.java.getResourceAsStream(
                                "RuneLite.theme.json",
                            ),
                        ),
                    ),
                )
            }
            installRsproxStyles()
            FlatLaf.updateUI()
            if (isCurrentLaf) {
                FlatAnimatedLafChange.hideSnapshotWithAnimation()
            }
        } catch (e: Throwable) {
            log.error(e) { "Failed to apply theme: $name" }
        }
    }

    private fun installRsproxStyles() {
        val ref = "\$"
        UIManager.put(
            "[style].rsproxHomeCard",
            "background: ${ref}Panel.background; border: 1,1,1,1,${ref}Button.borderColor",
        )
        UIManager.put(
            "[style].rsproxHomeCardHover",
            "background: ${ref}Button.hoverBackground; border: 1,1,1,1,${ref}Button.hoverBorderColor",
        )
        UIManager.put(
            "[style].rsproxHomeRow",
            "background: ${ref}Panel.background; border: 0,0,0,0",
        )
        UIManager.put(
            "[style].rsproxHomeRowHover",
            "background: ${ref}Button.hoverBackground; border: 0,0,0,0",
        )
        UIManager.put(
            "[style]Label.rsproxAccent",
            "foreground: ${ref}Button.focusedBorderColor",
        )
        UIManager.put(
            "[style]Label.rsproxMuted",
            "foreground: ${ref}Label.foreground",
        )
        UIManager.put(
            "[style]Button.rsproxLaunch",
            "background: ${ref}Button.focusedBorderColor; hoverBackground: tint(${ref}Button.focusedBorderColor,12%); pressedBackground: shade(${ref}Button.focusedBorderColor,12%); foreground: contrast(${ref}Button.focusedBorderColor,#1f1f1f,#fff); borderColor: ${ref}Button.focusedBorderColor; focusedBorderColor: ${ref}Button.focusedBorderColor; focusWidth: 1; innerFocusWidth: 0",
        )
        UIManager.put(
            "[style]Button.rsproxTargetImport",
            "foreground: ${ref}Button.focusedBorderColor; minimumWidth: 30; minimumHeight: 30; margin: 0,0,0,0; focusWidth: 1; innerFocusWidth: 0",
        )
        UIManager.put(
            "[style]Button.rsproxTabAction",
            "foreground: ${ref}Button.focusedBorderColor; background: ${ref}TabbedPane.background; hoverBackground: ${ref}Button.hoverBackground; pressedBackground: ${ref}Button.pressedBackground; borderColor: ${ref}TabbedPane.background; focusedBorderColor: ${ref}TabbedPane.background; minimumWidth: 62; minimumHeight: 26; margin: 0,8,0,8; focusWidth: 0; innerFocusWidth: 0",
        )
        UIManager.put(
            "[style]ToggleButton.rsproxFollow",
            "foreground: ${ref}Button.foreground; background: ${ref}Button.background; hoverBackground: ${ref}Button.hoverBackground; selectedBackground: ${ref}Button.focusedBorderColor; selectedForeground: contrast(${ref}Button.focusedBorderColor,#1f1f1f,#fff); borderColor: ${ref}Button.borderColor; focusedBorderColor: ${ref}Button.borderColor; minimumWidth: 82; minimumHeight: 28; focusWidth: 0; innerFocusWidth: 0",
        )
        UIManager.put(
            "[style]ComboBox.rsproxLaunch",
            "focusWidth: 1; innerFocusWidth: 0",
        )
    }
}
