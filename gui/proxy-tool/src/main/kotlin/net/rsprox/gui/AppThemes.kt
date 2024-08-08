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
            FlatLaf.updateUI()
            if (isCurrentLaf) {
                FlatAnimatedLafChange.hideSnapshotWithAnimation()
            }
        } catch (e: Throwable) {
            log.error(e) { "Failed to apply theme: $name" }
        }
    }
}
