package net.rsprox.patch.runelite

import com.github.michaelbull.logging.InlineLogger

public object ProcessManager {
    private val processNamePattern = Regex("""\.runelite/repository2/client-.*?-(\d+)-patched\.jar""")
    private val logger = InlineLogger()

    public fun findNewlyCreatedProcess(afterTimestamp: Long): String? {
        val os = System.getProperty("os.name").lowercase()
        try {
            val processList = if (os.contains("win")) {
                getWindowsProcesses()
            } else {
                getUnixProcesses()
            }

            processList.forEach { line ->
                val matchResult = processNamePattern.find(line)
                if (matchResult != null) {
                    val matchedTimestamp = matchResult.groupValues[1].toLong()
                    if (matchedTimestamp >= afterTimestamp) {
                        return extractPid(line, os)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load process list" }
        }

        return null
    }

    private fun getWindowsProcesses(): List<String> {
        val process = ProcessBuilder(
            "cmd.exe", "/c", "wmic process get ProcessId,ExecutablePath,CommandLine"
        ).start()
        return process.inputStream.bufferedReader().use { it.readLines() }
    }

    private fun getUnixProcesses(): List<String> {
        val process = ProcessBuilder("ps", "-eo", "pid,args").start()
        return process.inputStream.bufferedReader().use { it.readLines() }
    }

    private fun extractPid(line: String, os: String): String {
        return if (os.contains("win")) {
            // Windows: Last token is the PID
            line.split(Regex("\\s+")).last()
        } else {
            // Unix: First token is the PID
            line.split(Regex("\\s+"))[0]
        }
    }

    public fun killProcess(pid: String) {
        val os = System.getProperty("os.name").lowercase()

        if (os.contains("win")) {
            ProcessBuilder("taskkill", "/PID", pid, "/F").start().waitFor()
        } else {
            ProcessBuilder("kill", "-9", pid).start().waitFor()
        }
    }
}
