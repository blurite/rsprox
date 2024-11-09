import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadowjar)
}

dependencies {
    implementation(libs.bundles.flatlaf)
    implementation(libs.mig.layout)
    implementation(projects.proxy)
    implementation(libs.inline.logger)
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(projects.shared)
    implementation(libs.swingx)
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.codec.http)
}

tasks.processResources {
    filesMatching("**/rsprox.properties") {
        expand(
            "version" to project.version,
        )
    }
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("rsprox")
    archiveClassifier.set("")
    archiveVersion.set("")

    this.isZip64 = true
    mergeServiceFiles()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.rsprox.gui.ProxyToolGuiKt"
    }
}
