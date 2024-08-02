import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
import net.rsprox.gui.App
import java.util.*
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
