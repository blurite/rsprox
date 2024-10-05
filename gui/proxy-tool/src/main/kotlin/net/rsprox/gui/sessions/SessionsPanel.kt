package net.rsprox.gui.sessions

import com.formdev.flatlaf.extras.components.FlatTabbedPane
import net.rsprox.gui.App
import net.rsprox.shared.account.JagexCharacter
import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

public class SessionsPanel(
    private val app: App,
) : FlatTabbedPane() {
    private var counter = 0

    init {
        isTabsClosable = true
        isHasFullBorder = false
        border = BorderFactory.createEmptyBorder()
    }

    public fun createSession(
        type: SessionType,
        character: JagexCharacter?,
    ) {
        val session = SessionPanel(type, this, character)
        addTab("Session ${++counter}", type.icon, session, "")
        setTabCloseCallback(tabCount - 1) { tabbedPane, tabIndex ->
            val confirm =
                !session.isActive ||
                    JOptionPane.showConfirmDialog(
                        tabbedPane,
                        "Are you sure you want to close this session?",
                        "Close Session",
                        JOptionPane.YES_NO_OPTION,
                    ) == JOptionPane.YES_OPTION
            if (confirm) {
                session.kill()
                removeTabAt(tabIndex)
            }
        }
    }

    public fun syncSessionMetricsInfo(panel: SessionPanel) {
        if (panel !== selectedComponent) {
            return
        }
        val statusBar = app.statusBar
        SwingUtilities.invokeLater {
            val selectedSession = selectedComponent as SessionPanel?
            if (selectedSession != null) {
                val metrics = selectedSession.metrics
                statusBar.updateUser(metrics.username)
                statusBar.updateWorld(metrics.worldName)
                if (metrics.bandInPerSec == -1 && metrics.bandOutPerSec == -1) {
                    statusBar.hideBandwidth()
                } else {
                    statusBar.updateBandIn(metrics.bandInPerSec)
                    statusBar.updateBandOut(metrics.bandOutPerSec)
                }
            }
        }
    }

    public fun updateTabTitle(
        sessionPanel: SessionPanel,
        username: String,
    ) {
        val index = indexOfComponent(sessionPanel)
        if (index == -1) return
        setTitleAt(index, username)
    }
}
