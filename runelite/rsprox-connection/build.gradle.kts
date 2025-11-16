import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

plugins {
    id("java")
    id("org.jetbrains.kotlin.plugin.lombok") version "2.2.20"
    alias(libs.plugins.kotlin.jvm)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
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
    }
}

val runeLiteVersion = "latest.release"

@Suppress("VulnerableLibrariesLocal")
dependencies {
    implementation(libs.junixsocket)
    implementation(libs.kryo)
    compileOnly(libs.runelite.client)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    api(projects.protocol) {
        isTransitive = false
    }
    api(libs.rsprot.protocol) {
        isTransitive = false
    }
    api(libs.rsprot.crypto) {
        isTransitive = false
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(11)
}

val sourceSets = the<SourceSetContainer>()
val runtimeClasspath by configurations.getting

tasks.register<Jar>("shadowJar") {
    dependsOn(runtimeClasspath)

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets["main"].output)
    from(
        runtimeClasspath.map { file ->
            if (file.isDirectory) file else zipTree(file)
        },
    )

    exclude("META-INF/INDEX.LIST")
    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")
    exclude("**/module-info.class")
    exclude("META-INF/versions/**")

    group = BasePlugin.BUILD_GROUP
    archiveClassifier.set("shadow")
    archiveFileName.set("${project.name}-${rootProject.version}-all.jar")
}
