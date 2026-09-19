package net.rsprox.gui.sessions

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.gui.App
import java.awt.Component
import java.util.prefs.BackingStoreException
import java.util.prefs.Preferences
import javax.swing.JOptionPane

internal class Rs3LaunchWarning(
    private val preferences: Preferences =
        Preferences.userNodeForPackage(App::class.java).node("launchWarnings"),
    private val showDialog: (Component) -> Int = ::showWarning,
) {
    fun confirm(parent: Component): Boolean {
        if (preferences.getBoolean(ACKNOWLEDGED_KEY, false)) return true
        if (showDialog(parent) != 0) return false

        preferences.putBoolean(ACKNOWLEDGED_KEY, true)
        try {
            preferences.flush()
        } catch (exception: BackingStoreException) {
            logger.warn(exception) { "Unable to save the RuneScape 3 warning acknowledgement" }
        }
        return true
    }

    private companion object {
        private const val ACKNOWLEDGED_KEY = "rs3ExperimentalAcknowledged"
        private val logger = InlineLogger()

        private fun showWarning(parent: Component): Int {
            val options = arrayOf("I understand — Continue", "Cancel")
            return JOptionPane.showOptionDialog(
                parent,
                "RuneScape 3 support is new and experimental. There may be bugs.\n\n" +
                    "Sensitive-information removal may be incomplete. Recordings, transcripts\n" +
                    "and the GUI may still contain sensitive information. Review recordings\n" +
                    "and transcripts carefully before sharing them.\n\n" +
                    "Use at your own risk.",
                "RuneScape 3 — Experimental Support",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[1],
            )
        }
    }
}
