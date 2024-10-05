package net.rsprox.gui

import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatSeparator
import com.formdev.flatlaf.extras.components.FlatTabbedPane
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox
import net.miginfocom.swing.MigLayout
import net.rsprox.proxy.ProxyService
import net.rsprox.shared.settings.Setting
import net.rsprox.shared.settings.SettingCategory
import net.rsprox.shared.settings.SettingGroup
import java.awt.BorderLayout
import java.awt.event.ActionListener
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager

public class SettingsSidePanel(
    private val proxyService: ProxyService,
) : JPanel() {
    private val checkboxes = hashMapOf<Setting, JCheckBox>()
    private val settingsPanel = FiltersPanel(SettingGroup.LOGGING)

    init {
        layout = MigLayout("fill, ins panel, wrap 1, hidemode 3", "[grow]", "[grow, fill]")

        val tabbedGroup = FlatTabbedPane()
        for (group in SettingGroup.entries) {
            tabbedGroup.addTab(group.label, settingsPanel.wrapWithBorderlessScrollPane())
        }

        tabbedGroup.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (!SwingUtilities.isRightMouseButton(e)) return
                    val tabIndex = tabbedGroup.ui.tabForCoordinate(tabbedGroup, e.x, e.y)
                    if (tabIndex == -1) return
                    check(tabIndex == 0) {
                        "Other tabs not supported."
                    }
                    val panel = settingsPanel
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
        updateFilterState()
    }

    private fun updateFilterState() {
        val active = proxyService.settingsStore.getActive()
        for ((property, checkbox) in checkboxes) {
            checkbox.isSelected = active[property]
            checkbox.toolTipText = property.tooltip
        }
        settingsPanel.updateAllHeaderCheckboxes()
    }

    private inner class FiltersPanel(
        private val group: SettingGroup,
    ) : JPanel() {
        private val headerCheckboxes = hashMapOf<SettingCategory, FlatTriStateCheckBox>()

        init {
            layout = MigLayout("flowy, ins 0, gap 0", "[grow]", "[]")

            val filteredProperties =
                Setting.entries
                    .filter { it.group == group }
                    .groupBy { it.category }

            for ((category, properties) in filteredProperties) {
                add(createCategoryPanel(category, properties), "growx")
            }
        }

        private fun createCategoryPanel(
            category: SettingCategory,
            properties: List<Setting>,
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
            category: SettingCategory,
            properties: List<Setting>,
        ) = JPanel(BorderLayout()).apply {
            val toggle = FlatButton()
            toggle.toolTipText = "Collapse"
            toggle.icon = AppIcons.Collapse
            toggle.buttonType = FlatButton.ButtonType.toolBarButton

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
                val active = proxyService.settingsStore.getActive()
                for (property in properties) {
                    checkboxes[property]?.isSelected = checkbox.isSelected
                    active[property] = checkbox.isSelected
                }
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
            category: SettingCategory,
            properties: List<Setting>,
        ) {
            val checkbox = headerCheckboxes[category] ?: return

            val active = proxyService.settingsStore.getActive()
            val allSelected = properties.all { active[it] }
            val allUnselected = properties.none { active[it] }
            checkbox.state =
                when {
                    allSelected -> FlatTriStateCheckBox.State.SELECTED
                    allUnselected -> FlatTriStateCheckBox.State.UNSELECTED
                    else -> FlatTriStateCheckBox.State.INDETERMINATE
                }
        }

        fun updateAllHeaderCheckboxes() {
            for (category in headerCheckboxes.keys) {
                val properties = Setting.entries.filter { it.group == group && it.category == category }
                updateHeaderCheckbox(category, properties)
            }
        }

        private fun createPropertyFilterPanel(
            category: SettingCategory,
            properties: List<Setting>,
            property: Setting,
        ) = JPanel().apply {
            layout = BorderLayout()
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

            val checkbox = JCheckBox()
            checkbox.addActionListener {
                val active = proxyService.settingsStore.getActive()
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
            val active = proxyService.settingsStore.getActive()
            for ((property, checkbox) in checkboxes) {
                if (property.group != group) continue
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
