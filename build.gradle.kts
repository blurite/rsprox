import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

allprojects {
    group = "net.rsprox"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
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
