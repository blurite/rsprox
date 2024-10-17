import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.shadowjar)
}

dependencies {
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.bundles.log4j)
    implementation(libs.inline.logger)
    implementation(libs.okhttp3)
    implementation(libs.gson)
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("rsprox-launcher")
    archiveClassifier.set("")
    archiveVersion.set("")

    this.isZip64 = true
    mergeServiceFiles()
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "net.rsprox.launcher.LauncherKt"
    }
}
