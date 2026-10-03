dependencies {
    implementation(projects.proxy)
    implementation(projects.shared)
    implementation(projects.cache.cacheApi)
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.bundles.jackson)
    implementation(rootProject.libs.clikt)
    implementation(rootProject.libs.inline.logger)
}

tasks.register<JavaExec>("run") {
    environment("APP_VERSION", project.version)
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.mcp.McpMainKt")
}
