package net.rsprox.gui.components

import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatComboBox
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import net.rsprox.gui.auth.JagexAuthenticator
import net.rsprox.gui.sessions.SessionType
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.proxy.util.OperatingSystem
import net.rsprox.shared.account.JagexCharacter
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JList
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener
import javax.swing.plaf.basic.BasicComboPopup

public data class Character(
    val name: String,
)

public class LaunchBar(
    private val sessionsPanel: SessionsPanel,
) : JPanel() {
    private val sessionTypes =
        SessionType.entries
            .filter { type ->
                // Custom java is not yet offered
                if (type == SessionType.Java) {
                    return@filter false
                }
                // Mac native patcher is too fragile, so just disable the button on MacOS.
                if (App.service.operatingSystem == OperatingSystem.MAC && type == SessionType.Native) {
                    return@filter false
                }
                return@filter true
            }.toTypedArray()

    private val jagexAuthenticator = JagexAuthenticator()
    private val sessionTypesModel = DefaultComboBoxModel(sessionTypes)
    private val charactersModel = DefaultComboBoxModel<JagexCharacter>()

    init {
        layout = MigLayout("gap 10", "push[][][]", "[]")

        val characterDropdown = FlatComboBox<JagexCharacter>()
        characterDropdown.model = charactersModel
        characterDropdown.renderer = JagexCharacterCellRenderer()
        characterDropdown.minimumWidth = 160

        characterDropdown.addPopupMenuListener(
            object : PopupMenuListener {
                override fun popupMenuWillBecomeVisible(e: PopupMenuEvent?) {
                    val child = characterDropdown.getAccessibleContext().getAccessibleChild(0)
                    if (child is BasicComboPopup) {
                        child.removeAll()
                        val default =
                            JMenuItem("Default").apply {
                                addActionListener {
                                    characterDropdown.selectedItem = DEFAULT_CHARACTER
                                    App.service.jagexAccountStore.selectedCharacterId = null
                                }
                            }

                        child.add(default)

                        App.service.jagexAccountStore.accounts.forEach { account ->
                            if (child.subElements.size > 0) {
                                child.addSeparator()
                            }
                            account.characters.forEach { character ->
                                child.add(
                                    JMenuItem(character.safeDisplayName).apply {
                                        addActionListener {
                                            characterDropdown.selectedItem = character
                                            App.service.jagexAccountStore.selectedCharacterId = character.accountId
                                        }
                                    },
                                )
                            }
                        }
                        if (child.subElements.size > 0) {
                            child.addSeparator()
                        }
                        child.add(
                            JMenuItem("Manage Linked Accounts").apply {
                                addActionListener { showManageLinkedAccounts() }
                            },
                        )
                    }
                }

                override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {
                }

                override fun popupMenuCanceled(e: PopupMenuEvent?) {
                }
            },
        )
        add(characterDropdown, "growx")

        val launchModeDropdown =
            FlatComboBox<SessionType>().apply {
                model = sessionTypesModel
                renderer = SessionTypeCellRenderer()
            }

        launchModeDropdown.minimumWidth = 120
        add(launchModeDropdown, "growx")

        val launchButton =
            FlatButton().apply {
                toolTipText = "Launch"
                icon = AppIcons.Run
                buttonType = FlatButton.ButtonType.toolBarButton
                addActionListener { launchConfiguration() }
            }
        add(launchButton)

        refreshCharacters()
    }

    public fun refreshCharacters() {
        charactersModel.removeAllElements()

        charactersModel.addElement(DEFAULT_CHARACTER)

        App.service.jagexAccountStore.accounts
            .flatMap { it.characters }
            .forEach { charactersModel.addElement(it) }

        val selectedCharacterId = App.service.jagexAccountStore.selectedCharacterId
        if (selectedCharacterId == null) {
            charactersModel.selectedItem = DEFAULT_CHARACTER
        } else {
            charactersModel.selectedItem =
                App.service.jagexAccountStore.accounts
                    .flatMap { it.characters }
                    .find { it.accountId == selectedCharacterId }
        }
    }

    private fun launchConfiguration() {
        if (charactersModel.size == 0) {
            return
        }
        val sessionType = sessionTypesModel.selectedItem as SessionType
        val character =
            when (val character = charactersModel.selectedItem) {
                DEFAULT_CHARACTER -> null
                else -> character as JagexCharacter
            }
        sessionsPanel.createSession(sessionType, character)
    }

    private fun showManageLinkedAccounts() {
        val dialog = ManageLinkedAccountsDialog(jagexAuthenticator, this, sessionsPanel)
        dialog.isVisible = true
    }

    private class SessionTypeCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ) = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
            if (value is SessionType) {
                icon = value.icon
                text = value.name
            }
        }
    }

    private class JagexCharacterCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ) = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
            if (value is JagexCharacter) {
                text = value.safeDisplayName
            }
        }
    }

    private companion object {
        private val DEFAULT_CHARACTER = JagexCharacter(-1, "Default", -1L)
    }
}
