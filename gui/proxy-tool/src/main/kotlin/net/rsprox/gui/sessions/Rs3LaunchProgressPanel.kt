package net.rsprox.gui.sessions

import net.rsprox.proxy.rs3.Rs3LaunchProgress
import java.awt.BorderLayout
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.SwingUtilities
import javax.swing.Timer

/** Launch feedback stays outside the transcript and is independent for each session tab. */
internal class Rs3LaunchProgressPanel : JPanel(BorderLayout(0, 6)) {
    private val status = JLabel()
    private val bar = JProgressBar(0, 100)
    private val started = System.nanoTime()
    private var progress = Rs3LaunchProgress("Preparing RuneScape 3")
    private var finished = false
    private val timer = Timer(1000) { render() }

    init {
        border = BorderFactory.createEmptyBorder(10, 12, 10, 12)
        add(status, BorderLayout.NORTH)
        add(bar, BorderLayout.CENTER)
        add(JLabel("First launch may take longer while required files are downloaded."), BorderLayout.SOUTH)
        render()
    }

    fun update(value: Rs3LaunchProgress) {
        SwingUtilities.invokeLater {
            if (!finished) {
                progress = value
                render()
            }
        }
    }

    fun finish(success: Boolean) {
        SwingUtilities.invokeLater {
            if (finished) return@invokeLater
            finished = true
            timer.stop()
            bar.isIndeterminate = false
            if (success) {
                isVisible = false
            } else {
                bar.isVisible = false
                status.text = "Launch failed during: ${progress.stage}. See the application log for details."
            }
            revalidate()
            repaint()
        }
    }

    override fun addNotify() {
        super.addNotify()
        if (!finished) timer.start()
    }

    override fun removeNotify() {
        timer.stop()
        super.removeNotify()
    }

    private fun render() {
        val elapsed = (System.nanoTime() - started) / 1_000_000_000
        status.text = "${progress.stage} - ${elapsed}s elapsed"
        val percent = progress.percent
        bar.isIndeterminate = percent == null
        bar.isStringPainted = percent != null
        bar.value = percent ?: 0
        bar.string = percent?.let { "$it% of this stage" }
    }
}
