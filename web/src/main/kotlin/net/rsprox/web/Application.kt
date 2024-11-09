package net.rsprox.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists


@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(ApplicationProperties::class)
public class Application

private val CONFIGURATION_PATH: Path = Path(System.getProperty("user.home"), ".rsprox")
private val FOLDERS = listOf(
    "binary",
    "clients",
    "temp",
    "caches",
    "filters",
    "settings",
    "sockets",
    "signkey",
    "credentials",
    "binary.credentials",
    "runelite-launcher",
)

public fun main(args: Array<String>) {
    // create configuration folders necessary for the binary parsing
    createConfigurationFolders()

    runApplication<Application>(*args)
}

private fun createConfigurationFolders() {
    FOLDERS.forEach { folderName ->
        val folderPath = CONFIGURATION_PATH.resolve(folderName)
        if (!folderPath.exists()) {
            Files.createDirectories(folderPath)
        }
    }
}
