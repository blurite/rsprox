package net.rsprox.gui.components

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.extras.components.FlatButton
import com.formdev.flatlaf.util.Animator
import com.formdev.flatlaf.util.UIScale
import net.rsprox.gui.AppIcons
import java.awt.BasicStroke
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Cursor
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.LinearGradientPaint
import java.awt.RenderingHints
import java.awt.event.HierarchyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.Timer
import kotlin.math.PI
import kotlin.math.sin

internal class SponsorButton(
    onSponsor: () -> Unit,
) : JPanel(BorderLayout()) {
    private var animationFraction = 0f
    private var hovered = false
    private var hasAnimated = false
    private val delayTimer =
        Timer(INITIAL_DELAY) {
            if (!hovered) startAnimation()
        }.apply { isRepeats = false }
    private val animator =
        Animator(
            DURATION,
            { fraction ->
                animationFraction = fraction
                repaint()
            },
            {
                animationFraction = 0f
                repaint()
                scheduleAnimation(REPEAT_DELAY - DURATION)
            },
        ).apply { resolution = 16 }

    private val button =
        object : FlatButton() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)
                val progress = animationFraction * DURATION / 1000f
                if (progress <= 0f || progress >= 1f) return
                val graphics = g.create() as Graphics2D
                try {
                    graphics.clip(buttonOutline(width.toFloat(), height.toFloat()))
                    val sweepWidth = width * 0.45f
                    val center = width * (-0.3f + 1.6f * progress)
                    graphics.paint =
                        LinearGradientPaint(
                            center - sweepWidth / 2,
                            height.toFloat(),
                            center + sweepWidth / 2,
                            0f,
                            floatArrayOf(0f, 0.5f, 1f),
                            arrayOf(pink(0f), pink(0.26f * sin(PI * progress).toFloat()), pink(0f)),
                        )
                    graphics.fillRect(0, 0, width, height)
                } finally {
                    graphics.dispose()
                }
            }

            override fun paintBorder(g: Graphics) {
                super.paintBorder(g)
                if (animationFraction <= 0f) return
                val graphics = g.create() as Graphics2D
                try {
                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                    graphics.color = pink(bloom())
                    graphics.stroke = BasicStroke(UIScale.scale(1f))
                    graphics.draw(buttonOutline(width.toFloat(), height.toFloat()))
                } finally {
                    graphics.dispose()
                }
            }
        }.apply {
            text = "Sponsor on GitHub"
            icon =
                object : Icon {
                    override fun getIconWidth(): Int = AppIcons.Heart.iconWidth

                    override fun getIconHeight(): Int = AppIcons.Heart.iconHeight

                    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
                        val graphics = g.create() as Graphics2D
                        try {
                            val progress = (animationFraction * DURATION / 800f).coerceIn(0f, 1f)
                            val scale = 1.0 + 0.1 * sin(PI * progress)
                            graphics.translate(x + iconWidth / 2.0, y + iconHeight / 2.0)
                            graphics.scale(scale, scale)
                            graphics.translate(-iconWidth / 2.0, -iconHeight / 2.0)
                            AppIcons.Heart.paintIcon(c, graphics, 0, 0)
                        } finally {
                            graphics.dispose()
                        }
                    }
                }
            iconTextGap = 8
            cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            isRequestFocusEnabled = false
            putClientProperty(FlatClientProperties.STYLE_CLASS, "rsproxSponsor")
            addMouseListener(
                object : MouseAdapter() {
                    override fun mouseEntered(e: MouseEvent) {
                        hovered = true
                        stopAnimation()
                        startAnimation()
                    }

                    override fun mouseExited(e: MouseEvent) {
                        hovered = false
                        scheduleAnimation(REPEAT_DELAY)
                    }
                },
            )
            addActionListener {
                stopAnimation()
                onSponsor()
                scheduleAnimation(REPEAT_DELAY)
            }
        }

    init {
        isOpaque = false
        val padding = UIScale.scale(8)
        border = BorderFactory.createEmptyBorder(padding, padding, padding, padding)
        add(button, BorderLayout.CENTER)
        addHierarchyListener { event ->
            if (event.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong() != 0L) {
                if (isShowing) {
                    scheduleAnimation(if (hasAnimated) REPEAT_DELAY else INITIAL_DELAY)
                } else {
                    hovered = false
                    stopAnimation()
                }
            }
        }
    }

    override fun removeNotify() {
        hovered = false
        stopAnimation()
        super.removeNotify()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        if (animationFraction <= 0f) return
        val graphics = g.create() as Graphics2D
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            graphics.translate(button.x, button.y)
            val outline = buttonOutline(button.width.toFloat(), button.height.toFloat())
            for (radius in 7 downTo 1) {
                graphics.stroke = BasicStroke(UIScale.scale(radius * 2f))
                graphics.color = pink(bloom() * 0.035f)
                graphics.draw(outline)
            }
        } finally {
            graphics.dispose()
        }
    }

    private fun scheduleAnimation(delay: Int) {
        if (!isShowing || hovered || !Animator.useAnimation()) return
        delayTimer.initialDelay = delay
        delayTimer.restart()
    }

    private fun startAnimation() {
        if (!isShowing || !Animator.useAnimation()) return
        if (visibleRect.isEmpty) {
            scheduleAnimation(REPEAT_DELAY)
            return
        }
        stopAnimation()
        hasAnimated = true
        animator.start()
    }

    private fun stopAnimation() {
        delayTimer.stop()
        animator.cancel()
        animationFraction = 0f
        repaint()
    }

    private fun bloom(): Float = sin(PI * animationFraction).toFloat()

    private fun buttonOutline(width: Float, height: Float): RoundRectangle2D.Float {
        val inset = UIScale.scale(1.5f)
        val arc = UIScale.scale(12f)
        return RoundRectangle2D.Float(inset, inset, width - inset * 2, height - inset * 2, arc, arc)
    }

    private fun pink(alpha: Float): Color = Color(219, 97, 162, (alpha.coerceIn(0f, 1f) * 255).toInt())

    private companion object {
        private const val INITIAL_DELAY = 3000
        private const val REPEAT_DELAY = 10000
        private const val DURATION = 1800
    }
}
