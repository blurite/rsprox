package net.rsprox.gui.sessions

import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType
import com.formdev.flatlaf.extras.components.FlatTabbedPane
import com.formdev.flatlaf.extras.components.FlatToolBar
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import javax.swing.*

public class SessionsPanel(
    private val app: App
) : FlatTabbedPane() {

    private var counter = 0

    init {
        isTabsClosable = true
        isHasFullBorder = false
        border = BorderFactory.createEmptyBorder()
        trailingComponent = FlatToolBar().apply {
            isFloatable = false
            add(Box.createHorizontalGlue())
            add(FlatButton().apply {
                buttonType = ButtonType.tab
                icon = AppIcons.Add
                addActionListener {
                    val menu = JPopupMenu()
                    SessionType.entries.forEach { type ->
                        val item = JMenuItem(type.name).apply {
                            icon = type.icon
                            addActionListener {
                                createSession(type)
                            }
                        }
                        menu.add(item)
                    }
                    menu.show(this, width - menu.preferredSize.width, height)
                }
            })
        }
    }

    private fun createSession(type: SessionType) {
        addTab("Session ${++counter}", type.icon, SessionPanel(type, app, this), "")
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
                statusBar.updateBandIn(metrics.bandInPerSec)
                statusBar.updateBandOut(metrics.bandOutPerSec)
            }
        }
    }

    public fun updateTabTitle(sessionPanel: SessionPanel, username: String) {
        val index = indexOfComponent(sessionPanel)
        if (index == -1) return
        setTitleAt(index, username)
    }
}
