package net.rsprox.gui.dialogs

import java.awt.Component
import javax.swing.JOptionPane

public object Dialogs {
    public fun showInputString(
        parent: Component? = null,
        title: String,
        message: String,
        initialValue: String = "",
    ): String? {
        return JOptionPane.showInputDialog(
            parent,
            message,
            title,
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            initialValue,
        ) as String?
    }

    public fun showError(
        parent: Component? = null,
        title: String = "Error",
        message: String,
    ) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE)
    }
}
