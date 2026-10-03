// Resolves to the plugin's plain jar. It is shipped as a resource and installed before each launch.
val bridgePlugin: Configuration by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

dependencies {
    bridgePlugin(project(":runelite:rsprox-mcp-bridge"))
    implementation(projects.proxy)
    implementation(projects.shared)
    implementation(projects.cache.cacheApi)
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.bundles.jackson)
    implementation(rootProject.libs.clikt)
    implementation(rootProject.libs.inline.logger)
}

tasks.processResources {
    from(bridgePlugin) {
        rename { "rsprox-mcp-bridge.jar" }
    }
}

tasks.register<JavaExec>("run") {
    environment("APP_VERSION", project.version)
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.mcp.McpMainKt")
}
