import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.exists

dependencies {
    implementation(projects.protocol.osrs223)
    implementation(projects.transcriber)
    implementation(rootProject.libs.rsprot.protocol)
    implementation(projects.cache.cacheApi)
    implementation(projects.shared)
}

private val pluginsPath: Path = Path(System.getProperty("user.home"), ".rsprox", "transcribers")
if (!pluginsPath.exists()) {
    Files.createDirectories(pluginsPath)
}

subprojects {
    val project = this
    tasks.withType<Jar> {
        enabled = true
        isZip64 = true
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveFileName.set("${project.name}.jar")
        destinationDirectory.set(file(pluginsPath))

        from(sourceSets.main.get().output)
        dependsOn(configurations.compileClasspath)
        from({
            configurations.compileClasspath
                .get()
                .filter {
                    it.name.endsWith("jar")
                }.map { zipTree(it) }
        }) {
            exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
        }
    }
}
