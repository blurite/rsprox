package net.rsprox.gui

import net.rsprox.cache.resolver.HistoricCacheResolver
import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.cache.CachedCaches
import net.rsprox.proxy.cli.TranscribeCommand
import net.rsprox.proxy.util.TranscribeCallback
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.TransferHandler

public class FileDropHandler(
    public val service: ProxyService,
    private val transcriptionManager: TranscriptionManager,
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

        for (file in files) {
            if (!file.isFile || file.extension != "bin") {
                continue
            }
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

        return true
    }
}
