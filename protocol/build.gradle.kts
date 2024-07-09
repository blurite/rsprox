import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.rsprot.buffer)
    implementation(rootProject.libs.rsprot.compression)
    implementation(rootProject.libs.rsprot.protocol)
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.bundles.log4j)
}

private val pluginsPath: Path = Path(System.getProperty("user.home"), ".rsprox", "plugins")
if (!pluginsPath.exists()) {
    Files.createDirectories(pluginsPath)
}

subprojects {
    val project = this
    tasks.withType<Jar> {
        archiveFileName.set("${project.name}.jar")
        destinationDirectory.set(file(pluginsPath))
    }
}
