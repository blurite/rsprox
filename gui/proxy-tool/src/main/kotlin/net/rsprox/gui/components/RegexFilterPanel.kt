package net.rsprox.gui.components

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.extras.components.FlatButton.ButtonType
import com.formdev.flatlaf.extras.components.FlatCheckBox
import com.formdev.flatlaf.extras.components.FlatLabel
import com.formdev.flatlaf.extras.components.FlatTextField
import net.miginfocom.swing.MigLayout
import net.rsprox.gui.AppIcons
import net.rsprox.shared.filters.PropertyFilterSet
import net.rsprox.shared.filters.RegexFilter
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.regex.PatternSyntaxException
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.undo.UndoManager

public class RegexFilterPanel(
    private val filterSet: PropertyFilterSet,
    private var currentFilter: RegexFilter
) : JPanel() {

    private val protNameLabel = FlatLabel()
    private val regexTextField = FlatTextField()
    private val perLineCheckbox = FlatCheckBox()

    init {
        layout = MigLayout("gap 5", "[grow, fill][]", "[][][]")
        putClientProperty(FlatClientProperties.STYLE, "background: darken(${'$'}Panel.background,2%)")

        // Add a label for the filter name.
        protNameLabel.text = currentFilter.protName
        protNameLabel.toolTipText = "The name of the filter."
        installNameEditor(protNameLabel)
        add(protNameLabel)

        // Add a delete button to remove the filter.
        add(FlatButton().apply {
            toolTipText = "Delete"
            icon = AppIcons.Delete
            buttonType = ButtonType.borderless
            addActionListener {
                // Remove the filter from the filter set.
                filterSet.removeRegexFilter(currentFilter)

                // Remove the panel from the parent.
                val regexFilterPanel = this@RegexFilterPanel
                val parent = regexFilterPanel.parent
                parent.remove(regexFilterPanel)

                // Revalidate and repaint the parent.
                parent.revalidate()
                parent.repaint()
            }
        }, "wrap")

        add(JSeparator(), "span, wrap")

        // Add a text field for the regular expression.
        regexTextField.toolTipText = "The regular expression to match."
        regexTextField.placeholderText = "Regular expression"
        regexTextField.addActionListener { regexTextField.transferFocus() }
        regexTextField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) {
                regexUpdated()
            }

            override fun removeUpdate(e: DocumentEvent) {
                regexUpdated()
            }

            override fun changedUpdate(e: DocumentEvent) {
                regexUpdated()
            }
        })

        // Add undo/redo support to the text field.
        val undoManager = UndoManager()
        regexTextField.document.addUndoableEditListener(undoManager)
        regexTextField.addKeyListener(UndoRedoKeyListener(undoManager))

        // Set the columns to 1 to prevent the text field from growing and resizing the parent.
        regexTextField.columns = 1

        add(regexTextField)

        // Add a checkbox for matching per line.
        perLineCheckbox.toolTipText = "Whether to match per line of output."
        perLineCheckbox.addActionListener { saveFilter() }
        add(perLineCheckbox, "wrap")

        // Set the initial values based on the filter.
        protNameLabel.text = currentFilter.protName
        regexTextField.text = currentFilter.regex.pattern
        perLineCheckbox.isSelected = currentFilter.perLine
    }

    private fun installNameEditor(label: FlatLabel) {
        label.addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2) {
                    val editor = FlatTextField()
                    editor.text = label.text
                    editor.placeholderText = label.text
                    editor.border = null
                    // do not let it grow over the parent
                    editor.selectAll()

                    remove(label)
                    add(editor, 0)

                    revalidate()
                    repaint()

                    editor.addActionListener {
                        if (editor.text.isNotBlank()) {
                            label.text = editor.text
                        }
                        swapEditorWithLabel(editor, label)
                    }
                    editor.addFocusListener(object : FocusAdapter() {
                        override fun focusLost(e: FocusEvent) {
                            swapEditorWithLabel(editor, label)
                        }
                    })
                    editor.requestFocusInWindow()
                }
            }
        })
    }

    private fun saveFilter() {
        val oldRegexFilter = currentFilter
        val newRegexFilter = RegexFilter(
            protName = protNameLabel.text,
            regex = if (validateRegex(regexTextField.text)) Regex(regexTextField.text) else oldRegexFilter.regex,
            perLine = perLineCheckbox.isSelected
        )
        currentFilter = newRegexFilter
        filterSet.replaceRegexFilter(oldRegexFilter, newRegexFilter)
    }

    private fun swapEditorWithLabel(editor: JTextField, label: JLabel) {
        // Sync the label text with the editor text.
        label.text = editor.text
        saveFilter()

        // Remove the editor and add the label back.
        remove(editor)
        add(label, 0)

        // Revalidate and repaint the panel.
        revalidate()
        repaint()
    }

    private fun regexUpdated() {
        if (validateRegex(regexTextField.text)) {
            regexTextField.outline = null
            saveFilter()
        } else {
            regexTextField.outline = "error"
        }
    }

    private companion object {
        private fun validateRegex(regex: String): Boolean {
            return try {
                Regex(regex)
                true
            } catch (_: PatternSyntaxException) {
                false
            }
        }
    }

    private class UndoRedoKeyListener(private val undoManager: UndoManager) : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (e.isControlDown) {
                when {
                    e.keyCode == KeyEvent.VK_Z -> {
                        if (undoManager.canUndo()) {
                            undoManager.undo()
                        }
                    }
                    e.keyCode == KeyEvent.VK_Y -> {
                        if (undoManager.canRedo()) {
                            undoManager.redo()
                        }
                    }
                }
            }
        }
    }
}
