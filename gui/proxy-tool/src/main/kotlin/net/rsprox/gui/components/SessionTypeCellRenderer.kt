package net.rsprox.gui.components

import net.rsprox.gui.sessions.SessionType
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

internal class SessionTypeCellRenderer : DefaultListCellRenderer() {
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean,
    ) = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus).apply {
        if (value is SessionType) {
            icon = value.icon
            text = value.displayName
        }
    }
}
