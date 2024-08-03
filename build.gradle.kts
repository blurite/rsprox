import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    alias(libs.plugins.kotlin.jvm)
}

allprojects {
    group = "net.rsprox"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.openrs2.org/repository/openrs2-snapshots/")
        maven("https://maven.runelab.io/snapshots/")
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    plugins.withType<KotlinPluginWrapper> {
        dependencies {
            testImplementation(kotlin("test"))
        }

        tasks.test {
            useJUnitPlatform()
        }

        kotlin {
            jvmToolchain(11)
            explicitApi()
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
}

dependencies {
    runtimeOnly(projects.gui.proxyTool)
    runtimeOnly(projects.proxy)
}

tasks.create<JavaExec>("proxy") {
    environment("APP_VERSION", project.version)
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.gui.ProxyToolGuiKt")
}

tasks.create<JavaExec>("download") {
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.proxy.cli.ClientDownloadCommandKt")
}

tasks.create<JavaExec>("tostring") {
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.proxy.cli.BinaryToStringCommandKt")
}

tasks.create<JavaExec>("transcribe") {
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.proxy.cli.TranscribeCommandKt")
}

// fixes some weird error with "Entry classpath.index is a duplicate but no duplicate handling strategy has been set"
// see https://github.com/gradle/gradle/issues/17236
gradle.taskGraph.whenReady {
    val duplicateTasks =
        allTasks
            .filter { it.hasProperty("duplicatesStrategy") }
    for (task in duplicateTasks) {
        task.setProperty("duplicatesStrategy", "EXCLUDE")
    }
}
