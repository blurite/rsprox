package net.rsprox.gui.sidebar

import com.formdev.flatlaf.extras.components.FlatTabbedPane
import javax.swing.Icon
import javax.swing.JPanel

public class SideBar : FlatTabbedPane() {
    init {
        tabPlacement = RIGHT
        selectedIndex = -1
    }

    public fun addButton(
        icon: Icon,
        tooltip: String,
        content: JPanel,
    ) {
        addTab("", icon, content, tooltip)
    }

    override fun updateUI() {
        setUI(SideBarUI())
    }
}
