package net.rsprox.gui

import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatSeparator
import com.formdev.flatlaf.extras.components.FlatTabbedPane
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.dialogs.Dialogs
import net.rsprox.proxy.ProxyService
import net.rsprox.shared.StreamDirection
import net.rsprox.shared.filters.PropertyFilter
import net.rsprox.shared.filters.ProtCategory
import java.awt.BorderLayout
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.DefaultComboBoxModel
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager

public class FiltersSidePanel(
    private val proxyService: ProxyService,
) : JPanel() {
    private val presetsBoxModel = DefaultComboBoxModel<String>()
    private val presetsBox = JComboBox(presetsBoxModel)
    private val copyButton = createControlButton(AppIcons.Copy, "Copy selected preset into new preset")
    private val createButton = createControlButton(AppIcons.Add, "Create new preset from default filters")
    private val deleteButton = createControlButton(AppIcons.Delete, "Delete selected preset")
    private val checkboxes = hashMapOf<PropertyFilter, JCheckBox>()
    private val incomingPanel = FiltersPanel(StreamDirection.SERVER_TO_CLIENT)
    private val outgoingPanel = FiltersPanel(StreamDirection.CLIENT_TO_SERVER)

    init {
        layout = MigLayout("fill, ins panel, wrap 1, hidemode 3", "[grow]", "[][][][grow, fill]")

        presetsBox.addItemListener { e ->
            if (e.stateChange != ItemEvent.SELECTED) return@addItemListener
            proxyService.filterSetStore.setActive(presetsBox.selectedIndex)
            updateButtonState()
            updateFilterState()
        }

        createButton.addActionListener {
            val name =
                Dialogs.showInputString(parent = createButton, "Create new preset", "Enter preset name")
                    ?: return@addActionListener
            if (presetsBoxModel.getIndexOf(name) != -1) {
                Dialogs.showError(parent = this, message = "Preset with name '$name' already exists.")
                return@addActionListener
            }
            proxyService.filterSetStore.create(name)
            populatePresets()
            presetsBoxModel.selectedItem = name
        }

        deleteButton.addActionListener {
            val selectedIndex = presetsBox.selectedIndex
            if (selectedIndex == -1) return@addActionListener
            val presetName = presetsBox.selectedItem as String
            val result =
                JOptionPane.showConfirmDialog(
                    deleteButton,
                    "Are you sure you want to delete the selected preset?",
                    "Delete '$presetName' preset",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                )
            if (result != JOptionPane.YES_OPTION) return@addActionListener
            proxyService.filterSetStore.delete(selectedIndex)
            populatePresets()
        }

        copyButton.addActionListener {
            val selectedIndex = presetsBox.selectedIndex
            if (selectedIndex == -1) return@addActionListener
            val name =
                Dialogs.showInputString(parent = copyButton, "Copy preset", "Enter new preset name")
                    ?: return@addActionListener
            if (presetsBoxModel.getIndexOf(name) != -1) {
                Dialogs.showError(parent = this, message = "Preset with name '$name' already exists.")
                return@addActionListener
            }
            val filterSet = proxyService.filterSetStore.get(selectedIndex) ?: return@addActionListener
            val newFilterSet = proxyService.filterSetStore.create(name)
            for (property in PropertyFilter.entries) {
                newFilterSet[property] = filterSet[property]
            }
            populatePresets()
            presetsBoxModel.selectedItem = name
        }

        add(FlatLabel().apply { text = "Presets:" })

        add(presetsBox, "growx")

        val controlPanel =
            JPanel().apply {
                layout = MigLayout("insets 0", "[grow][grow][grow]", "[32px]")
                add(copyButton, "grow, hmin 32px")
                add(createButton, "grow, hmin 32px")
                add(deleteButton, "grow, hmin 32px")
            }

        add(controlPanel, "growx")

        val tabbedGroup = FlatTabbedPane()
        tabbedGroup.addTab("Incoming", incomingPanel.wrapWithBorderlessScrollPane())
        tabbedGroup.addTab("Outgoing", outgoingPanel.wrapWithBorderlessScrollPane())

        tabbedGroup.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (!SwingUtilities.isRightMouseButton(e)) return
                    if (presetsBox.selectedIndex == 0) return
                    val tabIndex = tabbedGroup.ui.tabForCoordinate(tabbedGroup, e.x, e.y)
                    if (tabIndex == -1) return
                    val panel = if (tabIndex == 0) incomingPanel else outgoingPanel
                    val menu = JPopupMenu()
                    val enableAll = JMenuItem("Enable All")
                    val disableAll = JMenuItem("Disable All")
                    enableAll.addActionListener { panel.setAll(true) }
                    disableAll.addActionListener { panel.setAll(false) }
                    menu.add(enableAll)
                    menu.add(disableAll)
                    menu.show(e.component, e.x, e.y)
                }
            },
        )

        add(tabbedGroup, "grow, pushy")

        populatePresets()
        updateButtonState()
        updateFilterState()
    }

    private fun updateFilterState() {
        val active = proxyService.filterSetStore.getActive()
        val selectedIndex = presetsBox.selectedIndex
        for ((property, checkbox) in checkboxes) {
            checkbox.isSelected = active[property]
            if (selectedIndex == 0) {
                checkbox.toolTipText = "Create a new preset to change the filters"
            } else {
                checkbox.toolTipText = property.tooltip
            }
        }
        incomingPanel.updateAllHeaderCheckboxes()
        outgoingPanel.updateAllHeaderCheckboxes()
    }

    private fun updateButtonState() {
        val selectedIndex = presetsBox.selectedIndex
        if (selectedIndex == -1) return
        deleteButton.isEnabled = selectedIndex != 0
        checkboxes.values.forEach { it.isEnabled = selectedIndex != 0 }
    }

    private fun populatePresets() {
        val oldSelectedItem = presetsBox.selectedItem
        presetsBoxModel.removeAllElements()
        for (i in 0 until proxyService.filterSetStore.size) {
            val filterSet = proxyService.filterSetStore.get(i) ?: continue
            presetsBoxModel.addElement(filterSet.getName())
        }
        if (oldSelectedItem != null) {
            val selectedIndex = presetsBoxModel.getIndexOf(oldSelectedItem)
            if (selectedIndex == -1) {
                presetsBox.selectedIndex = presetsBoxModel.size - 1
            } else {
                presetsBox.selectedIndex = selectedIndex
            }
        } else {
            presetsBox.selectedIndex = presetsBoxModel.size - 1
        }
    }

    private fun createControlButton(
        icon: Icon,
        tooltip: String,
    ) = FlatButton().apply {
        this.buttonType = ButtonType.square
        this.icon = icon
        this.toolTipText = tooltip
        isFocusPainted = false
    }

    private inner class FiltersPanel(
        private val direction: StreamDirection,
    ) : JPanel() {
        private val headerCheckboxes = hashMapOf<ProtCategory, FlatTriStateCheckBox>()

        init {
            layout = MigLayout("flowy, ins 0, gap 0", "[grow]", "[]")

            val filteredProperties =
                PropertyFilter.entries
                    .filter { it.direction == direction }
                    .groupBy { it.category }

            for ((category, properties) in filteredProperties) {
                add(createCategoryPanel(category, properties), "growx")
            }
        }

        private fun createCategoryPanel(
            category: ProtCategory,
            properties: List<PropertyFilter>,
        ) = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            val content = JPanel()
            content.border = null
            content.layout = BoxLayout(content, BoxLayout.Y_AXIS)
            for (property in properties) {
                content.add(createPropertyFilterPanel(category, properties, property))
            }

            add(createCategoryHeaderPanel(content, category, properties))
            add(FlatSeparator())

            add(content)

            updateHeaderCheckbox(category, properties)
        }

        private fun createCategoryHeaderPanel(
            content: JPanel,
            category: ProtCategory,
            properties: List<PropertyFilter>,
        ) = JPanel(BorderLayout()).apply {
            val toggle = FlatButton()
            toggle.toolTipText = "Collapse"
            toggle.icon = AppIcons.Collapse
            toggle.buttonType = ButtonType.toolBarButton

            val collapseAction =
                ActionListener {
                    content.isVisible = !content.isVisible
                    toggle.icon = if (content.isVisible) AppIcons.Collapse else AppIcons.Expand
                    toggle.toolTipText = if (content.isVisible) "Collapse" else "Expand"
                }

            toggle.addActionListener(collapseAction)
            add(toggle, BorderLayout.WEST)

            val label = FlatLabel()
            label.text = category.label
            label.labelType = FlatLabel.LabelType.large
            label.toolTipText = category.label
            add(label, BorderLayout.CENTER)

            val checkbox = FlatTriStateCheckBox()
            checkbox.border = BorderFactory.createEmptyBorder(0, 0, 0, 7)
            checkbox.isAllowIndeterminate = false

            checkbox.addActionListener {
                val active = proxyService.filterSetStore.getActive()
                for (property in properties) {
                    checkboxes[property]?.isSelected = checkbox.isSelected
                    active[property] = checkbox.isSelected
                }
                updateButtonState()
                updateHeaderCheckbox(category, properties)
            }
            add(checkbox, BorderLayout.EAST)
            headerCheckboxes[category] = checkbox

            label.addMouseListener(
                object : MouseAdapter() {
                    override fun mouseEntered(e: MouseEvent?) {
                        label.foreground = UIManager.getColor("Label.selectedForeground")
                    }

                    override fun mouseExited(e: MouseEvent?) {
                        label.foreground = UIManager.getColor("Label.foreground")
                    }

                    override fun mouseReleased(e: MouseEvent) {
                        if (SwingUtilities.isLeftMouseButton(e) &&
                            e.x >= 0 &&
                            e.x <= label.width &&
                            e.y >= 0 &&
                            e.y <= label.height
                        ) {
                            collapseAction.actionPerformed(null)
                        }
                    }
                },
            )
        }

        private fun updateHeaderCheckbox(
            category: ProtCategory,
            properties: List<PropertyFilter>,
        ) {
            val checkbox = headerCheckboxes[category] ?: return

            val active = proxyService.filterSetStore.getActive()
            val allSelected = properties.all { active[it] }
            val allUnselected = properties.none { active[it] }
            checkbox.isEnabled = presetsBox.selectedIndex != 0
            if (presetsBox.selectedIndex == 0) {
                checkbox.toolTipText = "Create a new preset to change the filters"
            } else {
                checkbox.toolTipText = null
            }
            checkbox.state =
                when {
                    allSelected -> FlatTriStateCheckBox.State.SELECTED
                    allUnselected -> FlatTriStateCheckBox.State.UNSELECTED
                    else -> FlatTriStateCheckBox.State.INDETERMINATE
                }
        }

        fun updateAllHeaderCheckboxes() {
            for (category in headerCheckboxes.keys) {
                val properties = PropertyFilter.entries.filter { it.direction == direction && it.category == category }
                updateHeaderCheckbox(category, properties)
            }
        }

        private fun createPropertyFilterPanel(
            category: ProtCategory,
            properties: List<PropertyFilter>,
            property: PropertyFilter,
        ) = JPanel().apply {
            layout = BorderLayout()
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

            val checkbox = JCheckBox()
            checkbox.addActionListener {
                val active = proxyService.filterSetStore.getActive()
                active[property] = checkbox.isSelected
                updateHeaderCheckbox(category, properties)
            }
            add(checkbox, BorderLayout.EAST)

            val label = FlatLabel()
            label.labelType = FlatLabel.LabelType.large
            label.text = property.label
            label.toolTipText = property.tooltip
            add(label, BorderLayout.CENTER)

            checkboxes[property] = checkbox
        }

        fun setAll(selected: Boolean) {
            val active = proxyService.filterSetStore.getActive()
            for ((property, checkbox) in checkboxes) {
                if (property.direction != direction) continue
                checkbox.isSelected = selected
                active[property] = selected
            }
            for ((_, headerCheckbox) in headerCheckboxes) {
                headerCheckbox.isSelected = selected
            }
        }
    }

    private companion object {
        private fun JComponent.wrapWithBorderlessScrollPane() =
            JScrollPane(this).apply {
                horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                verticalScrollBarPolicy = ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS
                verticalScrollBar.unitIncrement = 16

                border = null
            }
    }
}
