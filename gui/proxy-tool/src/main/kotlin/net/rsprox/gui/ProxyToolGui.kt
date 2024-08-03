package net.rsprox.gui

import java.util.Locale
import javax.swing.SwingUtilities

public fun main() {
    Locale.setDefault(Locale.US)
    SwingUtilities.invokeLater {
        val app = App()
        app.init()
        app.start()
    }
}
