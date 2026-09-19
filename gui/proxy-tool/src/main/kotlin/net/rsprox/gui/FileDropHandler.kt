package net.rsprox.gui

import net.rsprox.cache.resolver.HistoricCacheResolver
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.binary.isRuneScape3
import net.rsprox.proxy.binary.readBinaryHeader
import net.rsprox.proxy.cache.CachedCaches
import net.rsprox.proxy.cli.TranscribeCommand
import net.rsprox.proxy.util.TranscribeCallback
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.nio.file.Path
import javax.swing.JButton
import javax.swing.JOptionPane
import javax.swing.TransferHandler

public class FileDropHandler(
    public val service: ProxyService,
    private val transcriptionManager: TranscriptionManager,
    private val openReplay: (Path) -> Unit,
) : TransferHandler() {
    private val cachedCaches = CachedCaches(HistoricCacheResolver())

    override fun canImport(support: TransferSupport): Boolean {
        return support.dataFlavors.any(DataFlavor::isFlavorJavaFileListType)
    }

    override fun importData(support: TransferSupport): Boolean {
        if (!this.canImport(support)) {
            return false
        }

        @Suppress("UNCHECKED_CAST")
        val files =
            try {
                support.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<File>
            } catch (e: Exception) {
                return false
            }

        val binaryFiles = files.filter { it.isFile && it.extension.equals("bin", ignoreCase = true) }
        if (binaryFiles.isEmpty()) {
            return false
        }
        if (binaryFiles.size == 1) {
            val file = binaryFiles.single()
            val rs3 =
                try {
                    readBinaryHeader(file.toPath()).isRuneScape3()
                } catch (error: Exception) {
                    JOptionPane.showMessageDialog(
                        support.component,
                        "Unable to read binary header: ${error.message}",
                        "Open Binary Dump",
                        JOptionPane.ERROR_MESSAGE,
                    )
                    return false
                }
            if (rs3) {
                submitTranscription(file)
                return true
            }
            val pane = binaryOpenPane(file.name)
            val dialog = pane.createDialog(support.component, "Open Binary Dump")
            val choice =
                try {
                    dialog.isVisible = true
                    pane.value as? Int ?: -1
                } finally {
                    dialog.dispose()
                }
            when (choice) {
                0 -> {
                    openReplay(file.toPath())
                    return true
                }
                1 -> {
                    submitTranscription(file)
                    return true
                }
                else -> return false
            }
        }

        for (file in binaryFiles) {
            submitTranscription(file)
        }

        return true
    }

    private fun submitTranscription(file: File) {
        transcriptionManager.submit(file.nameWithoutExtension) {
            indeterminate("Preparing...")
            TranscribeCommand.transcribe(
                service,
                file,
                cachedCaches,
                object : TranscribeCallback {
                    private var lastPercent = -1

                    override fun indeterminate(note: String) {
                        this@submit.indeterminate(note)
                    }

                    override fun report(
                        percent: Int,
                        note: String,
                    ) {
                        // Avoid resyncing too often
                        if (percent == lastPercent) return
                        lastPercent = percent
                        this@submit.report(percent, note)
                    }

                    override fun isCancelled(): Boolean {
                        return this@submit.isCancelled()
                    }
                },
            )
            if (!isCancelled()) {
                report(100, "Complete")
            } else {
                indeterminate("Cancelled")
            }
        }
    }
}

internal fun binaryOpenPane(name: String): JOptionPane {
    val replay = JButton("Replay")
    val transcribe = JButton("Transcribe")
    val cancel = JButton("Cancel")
    val options = arrayOf(replay, transcribe, cancel)
    val pane =
        JOptionPane(
            "How would you like to open $name?",
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.DEFAULT_OPTION,
            null,
            options,
            replay,
        )
    options.forEachIndexed { index, button -> button.addActionListener { pane.value = index } }
    return pane
}
