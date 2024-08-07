package net.rsprox.gui

import net.rsprox.gui.splash.SplashScreen
import java.util.Locale
import javax.swing.ImageIcon
import javax.swing.SwingUtilities
import javax.swing.UIManager

public fun main() {
    Locale.setDefault(Locale.US)
    SplashScreen.init()
    SplashScreen.stage(0.0, "Preparing", "Setting up environment")
    App.service.start { percentage, actionText, subActionText, progressText ->
        SplashScreen.stage(percentage, actionText, subActionText, progressText)
    }
    SplashScreen.stop()
    SwingUtilities.invokeLater {
        // Disable icons in all trees.
        val emptyIcon = ImageIcon()
        UIManager.put("Tree.leafIcon", emptyIcon)
        UIManager.put("Tree.openIcon", emptyIcon)
        UIManager.put("Tree.closedIcon", emptyIcon)

        val app = App()
        app.init()
        app.start()
    }
}
