package net.rsprox.gui.components

import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatLabel
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import net.rsprox.gui.auth.JagexAuthenticator
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.shared.account.JagexAccount
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.DefaultListCellRenderer
import javax.swing.DefaultListModel
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JToolBar
import javax.swing.ListSelectionModel
import javax.swing.UIManager

public class ManageLinkedAccountsDialog(
    private val jagexAuthenticator: JagexAuthenticator,
    private val launchBar: LaunchBar,
    sessionsPanel: SessionsPanel,
) : JDialog() {
    private val accountsModel = DefaultListModel<JagexAccount>()

    init {
        title = "Manage Linked Accounts"
        isModal = true
        modalityType = ModalityType.APPLICATION_MODAL
        defaultCloseOperation = DISPOSE_ON_CLOSE
        isResizable = false

        layout = BorderLayout()
        size = Dimension(400, 300)

        val contentPanel =
            JPanel().apply {
                layout = BorderLayout()
                border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            }
        this.contentPane = contentPanel

        contentPanel.add(
            FlatLabel().apply {
                text = "Linked Accounts:"
                border = BorderFactory.createEmptyBorder(0, 0, 10, 0)
            },
            BorderLayout.NORTH,
        )

        val centerPanel =
            JPanel().apply {
                layout = BorderLayout()
                border = BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"))
            }
        val linkedAccountsPanel =
            JList<JagexAccount>(accountsModel).apply {
                cellRenderer = JagexAccountCellRenderer()
                selectionMode = ListSelectionModel.SINGLE_SELECTION
                isFocusable = false
                border = null
            }

        val toolBar =
            JToolBar().apply {
                isFloatable = false
                isRollover = true

                border = BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground"))

                val addButton =
                    FlatButton().apply {
                        icon = AppIcons.Add
                        toolTipText = "Link new account"
                        addActionListener { addNewAccount() }
                    }

                val removeButton =
                    FlatButton().apply {
                        icon = AppIcons.Remove
                        toolTipText = "Remove selected account"
                        addActionListener {
                            if (linkedAccountsPanel.selectedValue == null) {
                                return@addActionListener
                            }
                            App.service.jagexAccountStore.delete(linkedAccountsPanel.selectedValue)
                            refreshAccounts()
                        }
                    }

                add(addButton)
                add(removeButton)
            }

        centerPanel.add(toolBar, BorderLayout.NORTH)

        centerPanel.add(
            JScrollPane(linkedAccountsPanel).apply {
                border = null
            },
            BorderLayout.CENTER,
        )

        contentPanel.add(centerPanel, BorderLayout.CENTER)
        setLocationRelativeTo(sessionsPanel)
        refreshAccounts()
    }

    private fun addNewAccount() {
        jagexAuthenticator.ensureAuthServerOnline()
        val future = jagexAuthenticator.requestOAuth2()
        future.thenAccept { response ->
            val account = JagexAccount(response.code, response.idToken)
            App.service.jagexAccountStore.add(account)
            refreshAccounts()
        }
    }

    private fun refreshAccounts() {
        accountsModel.clear()
        accountsModel.addAll(App.service.jagexAccountStore.accounts)

        launchBar.refreshCharacters()
    }

    private class JagexAccountCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): Component {
            val account = value as JagexAccount
            val label = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel
            if (account.characters.isEmpty()) {
                val text = "No characters linked"
                label.text = text
                label.toolTipText = text
            } else {
                val text = account.characters.joinToString(", ") { it.safeDisplayName }
                label.text = truncateTextWithEllipsis(label, text)
                label.toolTipText = text
            }
            return label
        }

        private fun truncateTextWithEllipsis(
            label: JLabel,
            text: String,
        ): String {
            val fontMetrics = label.getFontMetrics(label.font)
            val availableWidth = 350
            if (fontMetrics.stringWidth(text) <= availableWidth) {
                return text
            }
            var truncatedText = text
            val ellipsis = "..."
            val ellipsisWidth = fontMetrics.stringWidth(ellipsis)
            while (fontMetrics.stringWidth(truncatedText) + ellipsisWidth > availableWidth &&
                truncatedText.isNotEmpty()
            ) {
                truncatedText = truncatedText.dropLast(1)
            }
            return "$truncatedText$ellipsis"
        }
    }
}
