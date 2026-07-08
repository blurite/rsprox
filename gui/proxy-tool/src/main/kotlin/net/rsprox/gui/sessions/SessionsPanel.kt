package net.rsprox.gui.sessions

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatTabbedPane
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Insets
import net.rsprox.shared.account.JagexCharacter
import javax.swing.BorderFactory
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

public class SessionsPanel(
    private val app: App,
) : FlatTabbedPane() {
    private var counter = 0
    private var homeComponent: Component? = null
    private var replayComponent: Component? = null

    public val sessionCount: Int
        get() = components.count { it is SessionPanel }

    init {
        isTabsClosable = true
        isHasFullBorder = false
        border = BorderFactory.createEmptyBorder()
        trailingComponent = createHomeButtonContainer()
    }

    public fun installHomePanel(component: Component) {
        if (homeComponent != null) {
            return
        }
        homeComponent = component
        addTab("", null, component, "Start a new session")
        val homeIndex = indexOfComponent(component)
        setTabClosable(homeIndex, false)
        setMinimumTabWidth(homeIndex, 0)
        setMaximumTabWidth(homeIndex, 0)
        setTabInsets(homeIndex, Insets(0, 0, 0, 0))
        setTabComponentAt(
            homeIndex,
            JPanel().apply {
                isOpaque = false
                preferredSize = Dimension(0, 0)
                minimumSize = preferredSize
                maximumSize = preferredSize
            },
        )
        selectedComponent = component
    }

    public fun installReplayPanel(component: Component) {
        if (replayComponent != null) {
            return
        }
        replayComponent = component
    }

    public fun showReplayPanel() {
        val component = replayComponent ?: return
        var index = indexOfComponent(component)
        if (index == -1) {
            addTab("Replay", AppIcons.Run, component, "Replay binary dumps")
            index = indexOfComponent(component)
            setTabClosable(index, false)
        }
        selectedComponent = component
        app.refreshSessionWorkspace()
    }

    public fun createSession(
        type: SessionType,
        character: JagexCharacter?,
    ) {
        val session = SessionPanel(type, this, character)
        addTab("Session ${++counter}", type.icon, session, "")
        selectedComponent = session
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
        app.refreshSessionWorkspace()
    }

    override fun removeTabAt(index: Int) {
        super.removeTabAt(index)
        app.refreshSessionWorkspace()
    }

    public fun syncSessionMetricsInfo(panel: SessionPanel) {
        if (panel !== selectedComponent) {
            return
        }
        val statusBar = app.statusBar
        SwingUtilities.invokeLater {
            val selectedSession = selectedComponent as? SessionPanel
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

    private fun selectHome() {
        val component = homeComponent ?: return
        selectedComponent = component
        app.refreshSessionWorkspace()
    }

    private fun createHomeButton(): FlatButton {
        return FlatButton().apply {
            icon = AppIcons.Add
            text = "New"
            toolTipText = "New Session"
            buttonType = FlatButton.ButtonType.toolBarButton
            isContentAreaFilled = true
            isBorderPainted = false
            isFocusable = false
            horizontalAlignment = SwingConstants.CENTER
            verticalAlignment = SwingConstants.CENTER
            horizontalTextPosition = SwingConstants.RIGHT
            verticalTextPosition = SwingConstants.CENTER
            iconTextGap = 6
            preferredSize = Dimension(TAB_ACTION_BUTTON_WIDTH, TAB_ACTION_BUTTON_HEIGHT)
            minimumSize = preferredSize
            maximumSize = preferredSize
            putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxTabAction")
            addActionListener { selectHome() }
        }
    }

    private fun createHomeButtonContainer(): JPanel {
        val button = createHomeButton()
        return JPanel(BorderLayout()).apply {
            isOpaque = false
            preferredSize = Dimension(TAB_ACTION_CONTAINER_WIDTH, TAB_ACTION_CONTAINER_HEIGHT)
            minimumSize = preferredSize
            add(button, BorderLayout.LINE_START)
        }
    }

    private companion object {
        private const val TAB_ACTION_BUTTON_WIDTH = 62
        private const val TAB_ACTION_BUTTON_HEIGHT = 26
        private const val TAB_ACTION_CONTAINER_WIDTH = 70
        private const val TAB_ACTION_CONTAINER_HEIGHT = 32
    }
}
