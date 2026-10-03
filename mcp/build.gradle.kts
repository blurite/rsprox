// Resolves to the plugin's plain jar. It is shipped as a resource and installed before each launch.
val bridgePlugin: Configuration by configurations.creating {
    isCanBeConsumed = false
    isTransitive = false
}

repositories {
    maven {
        url = uri("https://repo.runelite.net")
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
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

    // The bridge tests run the real plugin against the real server, with only the game faked.
    testImplementation(project(":runelite:rsprox-mcp-bridge"))
    testImplementation(rootProject.libs.runelite.client) {
        // Only what the plugin compiles against. The client's runtime graph adds the game and its natives.
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_API))
        }

        // The client asks for a classifier that the Guice version the proxy brings in does not publish.
        exclude(group = "com.google.inject", module = "guice")
    }
}

tasks.test {
    systemProperty("java.awt.headless", "true")

    // Code under test resolves ~/.rsprox from this property, so no test reads or writes the real one.
    systemProperty("user.home", temporaryDir.absolutePath)
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
