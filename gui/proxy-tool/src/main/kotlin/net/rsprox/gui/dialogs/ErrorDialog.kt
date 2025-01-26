package net.rsprox.gui.dialogs

import com.github.michaelbull.logging.InlineLogger
import net.rsprox.gui.SplashScreen
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EmptyBorder

@Suppress("SameParameterValue")
public class ErrorDialog private constructor(
    title: String,
    message: String,
) : JDialog() {
    private val rightColumn = JPanel()
    private val font = Font(Font.DIALOG, Font.PLAIN, 12)

    init {
        try {
            SplashScreen::class.java.getResourceAsStream("rsprox_128.png").use { stream ->
                setIconImage(ImageIO.read(stream))
            }
        } catch (e: IOException) {
            logger.error(e) {
                "Unable to load rsprox 128 image"
            }
        }
        try {
            SplashScreen::class.java.getResourceAsStream("rsprox_splash.png").use { stream ->
                val logo: BufferedImage = ImageIO.read(stream)
                val runelite = JLabel()
                runelite.setIcon(ImageIcon(logo))
                runelite.setAlignmentX(CENTER_ALIGNMENT)
                runelite.setBackground(DARK_GRAY_COLOR)
                runelite.setOpaque(true)
                rightColumn.add(runelite)
            }
        } catch (e: IOException) {
            logger.error(e) {
                "Unable to load rsprox splash image"
            }
        }
        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent) {
                    dispose()
                }
            },
        )
        setTitle(title)
        layout = BorderLayout()
        val pane = contentPane
        pane.setBackground(DARKER_GRAY_COLOR)
        val leftPane = JPanel()
        leftPane.setBackground(DARKER_GRAY_COLOR)
        leftPane.setLayout(BorderLayout())
        val titleComponent = JLabel("There was an error in RSProx")
        titleComponent.setForeground(Color.WHITE)
        titleComponent.setFont(font.deriveFont(16f))
        titleComponent.setBorder(EmptyBorder(10, 10, 10, 10))
        leftPane.add(titleComponent, BorderLayout.NORTH)
        leftPane.preferredSize = Dimension(400, 200)
        val textArea = JTextArea(message)
        textArea.setFont(font)
        textArea.setBackground(DARKER_GRAY_COLOR)
        textArea.setForeground(Color.LIGHT_GRAY)
        textArea.setLineWrap(true)
        textArea.setWrapStyleWord(true)
        textArea.setBorder(EmptyBorder(10, 10, 10, 10))
        textArea.isEditable = false
        leftPane.add(textArea, BorderLayout.CENTER)
        pane.add(leftPane, BorderLayout.CENTER)
        rightColumn.setLayout(BoxLayout(rightColumn, BoxLayout.Y_AXIS))
        rightColumn.setBackground(DARK_GRAY_COLOR)
        rightColumn.maximumSize = Dimension(200, Int.MAX_VALUE)
        pane.add(rightColumn, BorderLayout.EAST)
    }

    public fun open() {
        addButton("Exit") {
            dispose()
        }
        pack()
        SplashScreen.stop()
        setLocationRelativeTo(null)
        isVisible = true
    }

    private fun addButton(
        message: String,
        action: Runnable,
    ): ErrorDialog {
        val button = JButton(message)
        button.addActionListener { action.run() }
        button.setFont(font)
        button.setBackground(DARK_GRAY_COLOR)
        button.setForeground(Color.LIGHT_GRAY)
        button.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, DARK_GRAY_COLOR.brighter()),
                EmptyBorder(4, 4, 4, 4),
            ),
        )
        button.setAlignmentX(CENTER_ALIGNMENT)
        button.maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
        button.setFocusPainted(false)
        button.addChangeListener {
            if (button.model.isPressed) {
                button.setBackground(DARKER_GRAY_COLOR)
            } else if (button.model.isRollover) {
                button.setBackground(DARK_GRAY_HOVER_COLOR)
            } else {
                button.setBackground(DARK_GRAY_COLOR)
            }
        }
        rightColumn.add(button)
        rightColumn.revalidate()
        return this
    }

    public companion object {
        private val logger = InlineLogger()
        private val DARKER_GRAY_COLOR = Color(30, 30, 30)
        private val DARK_GRAY_COLOR = Color(40, 40, 40)
        private val DARK_GRAY_HOVER_COLOR = Color(35, 35, 35)

        public fun show(
            title: String,
            text: String,
        ) {
            val dialog = ErrorDialog(title, text)
            dialog.open()
        }
    }
}
