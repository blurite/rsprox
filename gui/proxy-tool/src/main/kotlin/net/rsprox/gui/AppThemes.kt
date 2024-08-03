package net.rsprox.gui

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.formdev.flatlaf.intellijthemes.FlatDraculaIJTheme
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*
import com.github.michaelbull.logging.InlineLogger
import java.awt.EventQueue
import javax.swing.UIManager

public object AppThemes {

    private val log = InlineLogger()

    private val DEFAULT_THEME = "MaterialDeepOcean" to { FlatMaterialDeepOceanIJTheme.setup() }

    public val THEMES: List<Pair<String, () -> Boolean>> = listOf(
        DEFAULT_THEME,
        "ArcDark" to { FlatArcDarkIJTheme.setup() },
        "AtomOneDark" to { FlatAtomOneDarkIJTheme.setup() },
        "AtomOneLight" to { FlatAtomOneLightIJTheme.setup() },
        "Dracula" to { FlatDraculaIJTheme.setup() },
        "GitHub Dark" to { FlatGitHubDarkIJTheme.setup() },
        "GitHub" to { FlatGitHubIJTheme.setup() },
        "LightOwl" to { FlatLightOwlIJTheme.setup() },
        "MaterialDarker" to { FlatMaterialDarkerIJTheme.setup() },
        "MaterialPalenight" to { FlatMaterialPalenightIJTheme.setup() },
        "MonokaiPro" to { FlatMonokaiProIJTheme.setup() },
        "NightOwl" to { FlatNightOwlIJTheme.setup() },
        "SolarizedDark" to { FlatSolarizedDarkIJTheme.setup() },
        "SolarizedLight" to { FlatSolarizedLightIJTheme.setup() }
    )

    public fun applyTheme(name: String) {
        EventQueue.invokeLater { applyThemeEdt(name) }
    }

    public fun applyThemeEdt(name: String) {
        check(EventQueue.isDispatchThread()) { "Must be called on the EDT" }
        try {
            val isCurrentLaf = UIManager.getLookAndFeel() is FlatLaf
            val lafChange = THEMES.firstOrNull { it.first == name } ?: DEFAULT_THEME
            if (isCurrentLaf) {
                FlatAnimatedLafChange.showSnapshot()
            }
            lafChange.second()
            FlatLaf.updateUI()
            if (isCurrentLaf) {
                FlatAnimatedLafChange.hideSnapshotWithAnimation()
            }
        } catch (e: Throwable) {
            log.error(e) { "Failed to apply theme: $name" }
        }
    }
}
