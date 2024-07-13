import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

rootProject.name = "rsprox"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "proxy",
    "protocol",
    "patch",
    "gui",
)

includeSubprojects(":protocol")
includeSubprojects(":patch")
includeSubprojects(":gui")

fun includeSubprojects(projectName: String) {
    val projectPath = project(projectName).projectDir.toPath()
    try {
        Files.walk(projectPath).filter(Files::isDirectory).forEach {
            searchSubproject(projectName, projectPath, it)
        }
    } catch (e: IOException) {
        System.err.println("Failed to walk plugin dir, skipping")
    }
}

fun searchSubproject(
    projectName: String,
    projectPath: Path,
    subprojectPath: Path,
) {
    val isMissingBuildFile = Files.notExists(subprojectPath.resolve("build.gradle.kts"))
    if (isMissingBuildFile) return
    val relativePath = projectPath.relativize(subprojectPath)
    val subprojectName = relativePath.toString().replace(File.separator, ":")
    include("$projectName:$subprojectName")
}
