package net.rsprox.gui

import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatSeparator
import net.miginfocom.swing.MigLayout
import javax.swing.Icon
import javax.swing.JPanel

public class StatusBar : JPanel() {
    private val userLabel = createIconLabel(AppIcons.User)
    private val worldLabel = createIconLabel(AppIcons.Web)
    private val bandInLabel =
        FlatLabel().apply {
            horizontalAlignment = FlatLabel.RIGHT
        }
    private val bandOutLabel =
        FlatLabel().apply {
            horizontalAlignment = FlatLabel.RIGHT
        }

    init {
        layout = MigLayout("ins 0 n n n", "[fill][fill]push[fill][fill]", "[bottom][]")

        add(FlatSeparator(), "spanx 4, wrap")

        add(userLabel)
        add(worldLabel)
        add(bandInLabel)
        add(bandOutLabel)

        updateUser("")
        updateWorld("")
        updateBandIn("")
        updateBandOut("")
    }

    public fun updateUser(text: String) {
        userLabel.text = text.ifBlank { DEFAULT_USER_TEXT }
    }

    public fun updateWorld(text: String) {
        worldLabel.isVisible = text.isNotBlank()
        worldLabel.text = text
    }

    public fun updateBandIn(bandIn: Int) {
        updateBandIn("Down: ${formatSpeed(bandIn)}")
    }

    private fun updateBandIn(text: String) {
        bandInLabel.isVisible = text.isNotBlank()
        bandInLabel.text = text
    }

    public fun updateBandOut(bandOut: Int) {
        updateBandOut("Up: ${formatSpeed(bandOut)}")
    }

    private fun updateBandOut(text: String) {
        bandOutLabel.isVisible = text.isNotBlank()
        bandOutLabel.text = text
    }

    public fun hideBandwidth() {
        bandInLabel.isVisible = false
        bandOutLabel.isVisible = false
    }

    private companion object {
        private const val DEFAULT_USER_TEXT = "Disconnected"

        private fun createIconLabel(icon: Icon): FlatLabel {
            val label = FlatLabel()
            label.icon = icon
            return label
        }

        private fun formatSpeed(speed: Int): String {
            return when {
                speed < 1024 -> "$speed B/S"
                speed < 1024 * 1024 -> "${speed / 1024} KB/S"
                else -> "${speed / (1024 * 1024)} MB/S"
            }
        }
    }
}
