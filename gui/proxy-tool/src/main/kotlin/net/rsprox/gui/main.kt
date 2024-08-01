import com.formdev.flatlaf.extras.FlatInspector
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMaterialDeepOceanIJTheme
import net.rsprox.gui.App
import javax.swing.SwingUtilities
import javax.swing.UIManager

public fun main() {
    FlatInspector.install("ctrl shift alt X")
    SwingUtilities.invokeLater {
        UIManager.put("Component.focusWidth", 0)

        FlatMaterialDeepOceanIJTheme.setup()

        val app = App()
        app.init()
        app.start()
    }
}
