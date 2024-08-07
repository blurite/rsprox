package net.rsprox.gui.splash

import com.github.michaelbull.logging.InlineLogger
import java.awt.Color
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.image.BufferedImage
import java.lang.reflect.InvocationTargetException
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.UIManager
import javax.swing.border.EmptyBorder
import javax.swing.plaf.basic.BasicProgressBarUI
import kotlin.concurrent.Volatile

public class SplashScreen private constructor() :
    JFrame(),
    ActionListener {
        private val action = JLabel("Loading")
        private val progress = JProgressBar()
        private val subAction = JLabel()
        private val timer: Timer

        @Volatile
        private var overallProgress = 0.0

        @Volatile
        private var actionText = "Loading"

        @Volatile
        private var subActionText = ""

        @Volatile
        private var progressText: String? = null

        init {
            setTitle("RSProx Launcher")
            setDefaultCloseOperation(EXIT_ON_CLOSE)
            isUndecorated = true
            SplashScreen::class.java.getResourceAsStream("rsprox_128.png").use { stream ->
                iconImage = ImageIO.read(stream)
            }
            layout = null
            val pane = contentPane
            pane.setBackground(DARKER_GRAY_COLOR)
            val font = Font(Font.DIALOG, Font.PLAIN, 12)
            var logo: BufferedImage?
            SplashScreen::class.java.getResourceAsStream("rsprox_splash.png").use { stream ->
                logo = ImageIO.read(stream)
            }
            val logoLabel = JLabel(ImageIcon(logo))
            pane.add(logoLabel)
            logoLabel.setBounds(0, 0, WIDTH, WIDTH)
            var y = WIDTH
            pane.add(action)
            action.setForeground(Color.WHITE)
            action.setBounds(0, y, WIDTH, 16)
            action.setHorizontalAlignment(SwingConstants.CENTER)
            action.setFont(font)
            y += action.height + PAD
            pane.add(progress)
            progress.setForeground(BRAND_ORANGE)
            progress.setBackground(BRAND_ORANGE.darker().darker())
            progress.setBorder(EmptyBorder(0, 0, 0, 0))
            progress.setBounds(0, y, WIDTH, 14)
            progress.setFont(font)
            progress.setUI(
                object : BasicProgressBarUI() {
                    override fun getSelectionBackground(): Color {
                        return Color.BLACK
                    }

                    override fun getSelectionForeground(): Color {
                        return Color.BLACK
                    }
                },
            )
            y += 12 + PAD
            pane.add(subAction)
            subAction.setForeground(Color.LIGHT_GRAY)
            subAction.setBounds(0, y, WIDTH, 16)
            subAction.setHorizontalAlignment(SwingConstants.CENTER)
            subAction.setFont(font)
            y += subAction.height + PAD
            setSize(WIDTH, y)
            setLocationRelativeTo(null)
            timer = Timer(100, this)
            timer.isRepeats = true
            timer.start()
            isVisible = true
        }

        override fun actionPerformed(e: ActionEvent) {
            action.setText(actionText)
            subAction.setText(subActionText)
            progress.maximum = 1000
            progress.setValue((overallProgress * 1000).toInt())
            val progressText = progressText
            if (progressText == null) {
                progress.setStringPainted(false)
            } else {
                progress.setStringPainted(true)
                progress.setString(progressText)
            }
        }

        public companion object {
            private val BRAND_ORANGE = Color(220, 138, 0)
            private val DARKER_GRAY_COLOR = Color(30, 30, 30)
            private const val WIDTH = 200
            private const val PAD = 10
            private var instance: SplashScreen? = null
            private val logger = InlineLogger()

            public fun init() {
                try {
                    SwingUtilities.invokeAndWait {
                        if (instance != null) {
                            return@invokeAndWait
                        }
                        try {
                            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
                            instance = SplashScreen()
                        } catch (e: Exception) {
                            logger.warn(e) {
                                "Unable to start splash screen"
                            }
                        }
                    }
                } catch (bs: InterruptedException) {
                    throw RuntimeException(bs)
                } catch (bs: InvocationTargetException) {
                    throw RuntimeException(bs)
                }
            }

            public fun stop() {
                SwingUtilities.invokeLater {
                    val instance = this.instance ?: return@invokeLater
                    instance.timer.stop()
                    // The CLOSE_ALL_WINDOWS quit strategy on MacOS dispatches WINDOW_CLOSING events to each frame
                    // from Window.getWindows. However, getWindows uses weak refs and relies on gc to remove windows
                    // from its list, causing events to get dispatched to disposed frames. The frames handle the events
                    // regardless of being disposed and will run the configured close operation. Set the close operation
                    // to DO_NOTHING_ON_CLOSE prior to disposing to prevent this.
                    instance.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE)
                    instance.dispose()
                    this.instance = null
                }
            }

            public fun stage(
                startProgress: Double,
                endProgress: Double,
                actionText: String?,
                subActionText: String,
                done: Int,
                total: Int,
                mib: Boolean,
            ) {
                val progress =
                    if (mib) {
                        val mibiBytes = (1024 * 1024).toDouble()
                        val ceil = 1.0 / 10.0
                        String.format("%.1f / %.1f MiB", done / mibiBytes, total / mibiBytes + ceil)
                    } else {
                        "$done / $total"
                    }
                stage(startProgress + (endProgress - startProgress) * done / total, actionText, subActionText, progress)
            }

            @JvmOverloads
            public fun stage(
                overallProgress: Double,
                actionText: String?,
                subActionText: String,
                progressText: String? = null,
            ) {
                val instance = this.instance ?: return println("Instance is null")
                instance.overallProgress = overallProgress
                if (actionText != null) {
                    instance.actionText = actionText
                }
                instance.subActionText = subActionText
                instance.progressText = progressText
            }
        }
    }
