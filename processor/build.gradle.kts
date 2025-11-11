dependencies {
    implementation(rootProject.libs.rsprot.protocol)
    implementation(projects.cache)
    implementation(projects.cache.cacheApi)
    implementation(projects.protocol)
    implementation(projects.proxy)
    implementation(projects.shared)
    findSubprojects(projects.protocol.name).forEach {
        implementation(it)
    }
}

fun findSubprojects(parentProject: String): List<Project> {
    return project(":$parentProject").subprojects.filter { it.buildFile.exists() }
}
