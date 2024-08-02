package net.rsprox.gui

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
import java.util.Locale
import javax.swing.SwingUtilities

public fun main() {
    Locale.setDefault(Locale.US)
    SwingUtilities.invokeLater {
        FlatMaterialDeepOceanIJTheme.setup()

        val app = App()
        app.init()
        app.start()
    }
}
