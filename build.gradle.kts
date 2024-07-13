import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    alias(libs.plugins.kotlin.jvm)
}

allprojects {
    group = "net.rsprox"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
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
    runtimeOnly(projects.proxy)
}

tasks.create<JavaExec>("proxy") {
    group = "run"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("net.rsprox.proxy.ProxyServiceKt")
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
