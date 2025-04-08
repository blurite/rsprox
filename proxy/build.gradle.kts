import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.toList

dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.codec.http)
    implementation(rootProject.libs.rsprot.buffer)
    implementation(rootProject.libs.rsprot.compression)
    implementation(rootProject.libs.rsprot.crypto)
    implementation(rootProject.libs.rsprot.protocol)
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.bundles.log4j)
    implementation(rootProject.libs.bundles.bouncycastle)
    implementation(rootProject.libs.java.jwt)
    implementation(rootProject.libs.bundles.jackson)
    implementation(projects.patch)
    implementation(projects.patch.patchNative)
    implementation(projects.protocol)
    implementation(projects.transcriber)
    implementation(rootProject.libs.clikt)
    implementation(rootProject.libs.classgraph)
    implementation(projects.cache)
    implementation(projects.cache.cacheApi)
    implementation(projects.shared)
    implementation(libs.junixsocket)
    implementation(libs.okhttp3)
    implementation(libs.gson)
    implementation(projects.protocol.osrs223)
    implementation(projects.protocol.osrs224)
    implementation(projects.protocol.osrs225)
    implementation(projects.protocol.osrs226)
    implementation(projects.protocol.osrs227)
    implementation(projects.protocol.osrs228)
    implementation(projects.protocol.osrs229)
    implementation(projects.protocol.osrs230)
}

tasks.build.configure {
    finalizedBy(buildPluginTaskDependencies().toTypedArray())
}

tasks.compileKotlin.configure {
    finalizedBy(buildPluginTaskDependencies().toTypedArray())
}

fun buildPluginTaskDependencies(): List<String> {
    val subprojects =
        buildSubprojectTree(":protocol")
            .plus(buildSubprojectTree(":transcriber"))
    val buildTasks = subprojects.map { it.removeSuffix(":") + ":build" }
    // Get rid of the projects we already directly depend on in the above
    // dependencies block
    val filteredTasks =
        buildTasks.filterNot { task ->
            task == ":protocol:build" ||
                task == ":transcriber:build"
        }
    return filteredTasks
}

fun buildSubprojectTree(projectName: String): List<String> {
    val projectPath = rootProject.project(projectName).projectDir.toPath()
    return try {
        Files.walk(projectPath).filter(Files::isDirectory).toList().mapNotNull {
            searchSubproject(projectName, projectPath, it)
        }
    } catch (e: IOException) {
        emptyList()
    }
}

fun searchSubproject(
    projectName: String,
    projectPath: Path,
    subprojectPath: Path,
): String? {
    val isMissingBuildFile = Files.notExists(subprojectPath.resolve("build.gradle.kts"))
    if (isMissingBuildFile) return null
    val relativePath = projectPath.relativize(subprojectPath)
    val subprojectName = relativePath.toString().replace(File.separator, ":")
    return "$projectName:$subprojectName"
}
