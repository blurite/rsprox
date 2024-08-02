dependencies {
    implementation(projects.gui)
    implementation(libs.bundles.flatlaf)
    implementation(libs.mig.layout)
    implementation(projects.proxy)
    implementation(libs.inline.logger)
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(projects.shared)
    implementation(libs.swingx)
}

tasks.processResources {
    filesMatching("**/rsprox.properties") {
        expand(
            "version" to project.version,
        )
    }
}
