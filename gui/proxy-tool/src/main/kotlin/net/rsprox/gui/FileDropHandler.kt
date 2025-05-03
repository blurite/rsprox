package net.rsprox.gui

import net.rsprox.proxy.ProxyService
import net.rsprox.proxy.cli.TranscribeCommand
import java.awt.datatransfer.DataFlavor
import java.io.File
import javax.swing.TransferHandler

public class FileDropHandler(
    public val service: ProxyService,
) : TransferHandler() {
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
            TranscribeCommand.transcribe(service, file)
        }

        return true
    }
}
