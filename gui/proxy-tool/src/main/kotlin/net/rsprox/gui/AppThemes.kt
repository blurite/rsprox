package net.rsprox.gui

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.*

public object AppThemes {
    private val DEFAULT_THEME = "MaterialDeepOcean" to {FlatMaterialDeepOceanIJTheme.setup()}

    public val THEMES: List<Pair<String, () -> Boolean>> = listOf(
        DEFAULT_THEME,
        "ArcDark" to {FlatArcDarkIJTheme.setup()},
        "AtomOneDark" to {FlatAtomOneDarkIJTheme.setup()},
        "AtomOneLight" to {FlatAtomOneLightIJTheme.setup()},
        "Dracula" to {FlatDraculaIJTheme.setup()},
        "GitHub Dark" to {FlatGitHubDarkIJTheme.setup()},
        "GitHub" to {FlatGitHubIJTheme.setup()},
        "LightOwl" to {FlatLightOwlIJTheme.setup()},
        "MaterialDarker" to {FlatMaterialDarkerIJTheme.setup()},
        "MaterialPalenight" to {FlatMaterialPalenightIJTheme.setup()},
        "MonokaiPro" to {FlatMonokaiProIJTheme.setup()},
        "NightOwl" to {FlatNightOwlIJTheme.setup()},
        "SolarizedDark" to {FlatSolarizedDarkIJTheme.setup()},
        "SolarizedLight" to {FlatSolarizedLightIJTheme.setup()}
    )

    public fun applyTheme(name: String, fn: () -> Unit) {
        val theme = THEMES.firstOrNull { it.first == name } ?: DEFAULT_THEME
        theme.second.invoke()
        fn.invoke()
    }
}
