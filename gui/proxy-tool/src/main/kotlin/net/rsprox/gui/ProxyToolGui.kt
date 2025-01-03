package net.rsprox.gui

import java.util.Locale
import javax.swing.ImageIcon
import javax.swing.SwingUtilities
import javax.swing.UIManager

public fun main(args: Array<String>) {
    val rspsJavConfigUrl = System.getenv("RSPS_JAVCONFIG_URL")
    val rspsModulus = System.getenv("RSPS_RSA")

    when {
        rspsModulus == null && rspsJavConfigUrl != null -> throw IllegalArgumentException(
            "Missing ENV variable: RSPS_RSA",
        )

        rspsModulus != null && rspsJavConfigUrl == null -> throw IllegalArgumentException(
            "Missing ENV variable: RSPS_JAVCONFIG_URL",
        )
    }

    if (args.isNotEmpty()) {
        System.setProperty("net.rsprox.gui.args", args.joinToString("|"))
    }
    Locale.setDefault(Locale.US)
    SplashScreen.init()
    SplashScreen.stage(0.0, "Preparing", "Setting up environment")
    App.service.start(rspsJavConfigUrl, rspsModulus) { percentage, actionText, subActionText, progressText ->
        SplashScreen.stage(percentage, actionText, subActionText, progressText)
    }
    SplashScreen.stop()
    SwingUtilities.invokeLater {
        // Disable icons in all trees.
        val emptyIcon = ImageIcon()
        UIManager.put("Tree.leafIcon", emptyIcon)
        UIManager.put("Tree.openIcon", emptyIcon)
        UIManager.put("Tree.closedIcon", emptyIcon)
        UIManager.put("Component.focusWidth", 0)
        UIManager.put("Component.innerOutlineWidth", 0)

        val app = App()
        app.init()
        // Apply the theme here, as applying it too early causes some artefacting
        // This ensures that the theme you see is the same on initial boot as well as swapping back and forth
        AppThemes.applyThemeEdt(App.service.getAppTheme())
        app.start()
    }
}
