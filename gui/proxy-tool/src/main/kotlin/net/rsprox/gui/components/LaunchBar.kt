package net.rsprox.gui.components

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatComboBox
import com.formdev.flatlaf.util.SystemFileChooser
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.App
import net.rsprox.gui.AppIcons
import net.rsprox.gui.auth.JagexAuthenticator
import net.rsprox.gui.sessions.SessionType
import net.rsprox.gui.sessions.SessionsPanel
import net.rsprox.proxy.target.ProxyTargetConfig
import net.rsprox.proxy.target.ProxyTargetImportResult
import net.rsprox.proxy.util.OperatingSystem
import net.rsprox.shared.account.JagexCharacter
import java.awt.Dimension
import java.net.MalformedURLException
import java.net.URL
import javax.swing.DefaultComboBoxModel
import javax.swing.DefaultListCellRenderer
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.SwingConstants
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
        layout =
            MigLayout(
                "ins 0, gapx 12, gapy 8, fillx",
                "[fill, grow 50][fill, grow 50]",
                "[][][][]",
            )
        isOpaque = false
        val targetConfigs = App.service.proxyTargets
        val targetConfigsModel = DefaultComboBoxModel(targetConfigs.toTypedArray())
        val proxyTargetDropdown =
            FlatComboBox<ProxyTargetConfig>().apply {
                model = targetConfigsModel
                renderer = ProxyTargetCellRenderer()
                selectedIndex = App.service.getSelectedProxyTarget()
                configureComboBox()
            }
        proxyTargetDropdown.addActionListener {
            App.service.setSelectedProxyTarget(proxyTargetDropdown.selectedIndex)
        }

        val importTargetsButton =
            ImportTargetButton().apply {
                toolTipText = "Import Proxy Targets"
                addActionListener { importProxyTargets() }
            }

        val characterDropdown =
            FlatComboBox<JagexCharacter>().apply {
                model = charactersModel
                renderer = JagexCharacterCellRenderer()
                configureComboBox()
            }

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
                        sizeAccountPopup(characterDropdown, child)
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
                selectedIndex = App.service.getSelectedClient()
                configureComboBox()
            }
        launchModeDropdown.addActionListener {
            App.service.setSelectedClient(launchModeDropdown.selectedIndex)
        }

        val launchButton =
            LaunchSessionButton().apply {
                text = "Launch Session"
                toolTipText = "Launch Session"
                addActionListener { launchConfiguration() }
            }

        add(createFieldLabel("Account"))
        add(createFieldLabel("Client Type"), "wrap")
        add(characterDropdown, "growx, h $CONTROL_HEIGHT!")
        add(launchModeDropdown, "growx, h $CONTROL_HEIGHT!, wrap")
        add(createFieldLabel("Proxy Target"), "spanx 2, growx, gaptop 4, wrap")
        add(proxyTargetDropdown, "growx, h $CONTROL_HEIGHT!, spanx 2, split 2")
        add(importTargetsButton, "w $CONTROL_HEIGHT!, h $CONTROL_HEIGHT!, wrap")
        add(launchButton, "spanx 2, growx, h 34!, gaptop 8")

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

    public fun launchConfiguration() {
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

    private fun importProxyTargets() {
        val options = arrayOf("From File...", "From URL...")
        val choice =
            JOptionPane.showOptionDialog(
                this,
                "How would you like to import proxy targets?",
                "Import Proxy Targets",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options.first(),
            )

        when (choice) {
            0 -> importProxyTargetsFromFile()
            1 -> importProxyTargetsFromUrl()
        }
    }

    private fun importProxyTargetsFromFile() {
        val chooser = SystemFileChooser()
        chooser.fileSelectionMode = SystemFileChooser.FILES_ONLY
        val result = chooser.showOpenDialog(this)
        if (result != SystemFileChooser.APPROVE_OPTION) {
            return
        }

        val selectedFile = chooser.selectedFile ?: return
        attemptImport { App.service.importProxyTargets(selectedFile.toPath()) }
    }

    private fun importProxyTargetsFromUrl() {
        val input =
            JOptionPane.showInputDialog(
                this,
                "Enter the URL of a proxy target YAML file:",
                "Import Proxy Targets",
                JOptionPane.QUESTION_MESSAGE,
            ) ?: return

        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return
        }

        try {
            val url = URL(trimmed)
            attemptImport { App.service.importProxyTargets(url) }
        } catch (malformed: MalformedURLException) {
            JOptionPane.showMessageDialog(
                this,
                malformed.message ?: "The provided URL is invalid.",
                "Import Failed",
                JOptionPane.ERROR_MESSAGE,
            )
        }
    }

    private fun attemptImport(action: () -> ProxyTargetImportResult) {
        try {
            val importResult = action()
            showImportSummary(importResult)
        } catch (iae: IllegalArgumentException) {
            JOptionPane.showMessageDialog(
                this,
                iae.message ?: "Unable to import proxy targets.",
                "Import Failed",
                JOptionPane.ERROR_MESSAGE,
            )
        } catch (t: Throwable) {
            JOptionPane.showMessageDialog(
                this,
                t.message ?: "Unable to import proxy targets.",
                "Import Failed",
                JOptionPane.ERROR_MESSAGE,
            )
        }
    }

    private fun showImportSummary(result: ProxyTargetImportResult) {
        val summaryParts = mutableListOf<String>()
        if (result.addedCount > 0) {
            summaryParts +=
                if (result.addedCount == 1) "1 new target added" else "${result.addedCount} new targets added"
        }
        if (result.replacedCount > 0) {
            summaryParts +=
                if (result.replacedCount == 1) "1 target updated" else "${result.replacedCount} targets updated"
        }
        if (summaryParts.isEmpty()) {
            summaryParts += "No targets were imported."
        }

        val message = StringBuilder(summaryParts.joinToString(separator = "\n"))
        if (result.skippedTargets.isNotEmpty()) {
            message.append('\n')
            message.append('\n')
            message.append("Skipped entries: ")
            message.append(result.skippedTargets.joinToString())
        }
        message.append('\n')
        message.append('\n')
        message.append("Targets file: ")
        message.append(result.destination)
        message.append('\n')
        message.append("Restart RSProx to use the updated targets.")

        val messageType =
            if (result.skippedTargets.isEmpty()) {
                JOptionPane.INFORMATION_MESSAGE
            } else {
                JOptionPane.WARNING_MESSAGE
            }

        JOptionPane.showMessageDialog(
            this,
            message.toString(),
            "Proxy Targets Import",
            messageType,
        )
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

    private class ProxyTargetCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ) = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
            if (value is ProxyTargetConfig) {
                text =
                    if (value.revision != null && value.revision != "latest_supported") {
                        "${value.name} (${value.revision})"
                    } else {
                        value.name
                    }
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

    private class LaunchSessionButton : FlatButton() {
        override fun updateUI() {
            super.updateUI()
            applyLaunchButtonStyle(this)
        }
    }

    private class ImportTargetButton : FlatButton() {
        override fun updateUI() {
            super.updateUI()
            applyImportButtonStyle(this)
        }
    }

    private companion object {
        private const val CONTROL_HEIGHT = 30
        private val DEFAULT_CHARACTER = JagexCharacter(-1, "Default", -1L)

        private fun createFieldLabel(text: String): JLabel {
            return JLabel(text)
        }

        private fun <T> FlatComboBox<T>.configureComboBox() {
            minimumWidth = 120
            maximumRowCount = 12
            putClientProperty(FlatClientProperties.STYLE_CLASS, STYLE_LAUNCH_COMBO)
        }

        private fun sizeAccountPopup(
            comboBox: FlatComboBox<JagexCharacter>,
            popup: BasicComboPopup,
        ) {
            val preferred = popup.preferredSize
            val width = maxOf(comboBox.width, preferred.width)
            popup.preferredSize = Dimension(width, preferred.height)
            popup.minimumSize = Dimension(comboBox.width, preferred.height)
            popup.setPopupSize(width, preferred.height)
        }

        private fun applyLaunchButtonStyle(button: FlatButton) {
            button.icon = null
            button.buttonType = FlatButton.ButtonType.square
            button.isContentAreaFilled = true
            button.putClientProperty(FlatClientProperties.STYLE_CLASS, STYLE_LAUNCH_BUTTON)
        }

        private fun applyImportButtonStyle(button: FlatButton) {
            button.text = null
            button.icon = AppIcons.Add
            button.horizontalAlignment = SwingConstants.CENTER
            button.verticalAlignment = SwingConstants.CENTER
            button.horizontalTextPosition = SwingConstants.CENTER
            button.verticalTextPosition = SwingConstants.CENTER
            button.iconTextGap = 0
            button.buttonType = FlatButton.ButtonType.square
            button.isContentAreaFilled = true
            button.preferredSize = Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT)
            button.minimumSize = Dimension(CONTROL_HEIGHT, CONTROL_HEIGHT)
            button.putClientProperty(FlatClientProperties.STYLE_CLASS, STYLE_IMPORT_BUTTON)
        }

        private const val STYLE_LAUNCH_BUTTON = "rsproxLaunch"
        private const val STYLE_IMPORT_BUTTON = "rsproxTargetImport"
        private const val STYLE_LAUNCH_COMBO = "rsproxLaunch"
    }
}
