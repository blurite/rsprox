package net.rsprox.gui

import io.netty.buffer.ByteBufAllocator
import net.rsprox.gui.splash.SplashScreen
import net.rsprox.proxy.ProxyService
import java.util.Locale
import javax.swing.ImageIcon
import javax.swing.SwingUtilities
import javax.swing.UIManager

public fun main() {
    Locale.setDefault(Locale.US)
    SplashScreen.init()
    SplashScreen.stage(0.0, "Preparing", "Setting up environment")
    val service = ProxyService(ByteBufAllocator.DEFAULT)
    service.start { percentage, actionText, subActionText, progressText ->
        SplashScreen.stage(percentage, actionText, subActionText, progressText)
    }
    SplashScreen.stop()
    SwingUtilities.invokeLater {
        // Disable icons in all trees.
        val emptyIcon = ImageIcon()
        UIManager.put("Tree.leafIcon", emptyIcon)
        UIManager.put("Tree.openIcon", emptyIcon)
        UIManager.put("Tree.closedIcon", emptyIcon)

        val app = App(service)
        app.init()
        app.start()
    }
}
