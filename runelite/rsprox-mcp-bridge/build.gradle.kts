import java.util.zip.ZipFile

plugins {
    id("java")
}

repositories {
    maven {
        url = uri("https://repo.runelite.net")
        content {
            includeGroupByRegex("net\\.runelite.*")
        }
    }
    mavenCentral()
}

dependencies {
    // The client provides its API, Gson, javax.inject and slf4j at runtime; the jar bundles nothing.
    compileOnly(libs.runelite.client)
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(11)
}

tasks.jar {
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

// The root build applies the Kotlin plugin to every subproject. A sideloaded plugin gets its own class
// loader, so anything that leaks into this jar would shadow or duplicate what the client already has.
val verifyJarContents by tasks.registering {
    val jar = tasks.jar.flatMap { it.archiveFile }
    inputs.file(jar)
    doLast {
        val own = "net/rsprox/mcpbridge/"
        val foreign =
            ZipFile(jar.get().asFile).use { zip ->
                zip
                    .entries()
                    .asSequence()
                    .map { it.name }
                    .filterNot { it.startsWith(own) || own.startsWith(it) }
                    .filterNot { it == "META-INF/" || it == "META-INF/MANIFEST.MF" }
                    .toList()
            }
        check(foreign.isEmpty()) { "The bridge plugin jar must hold only its own classes, but found: $foreign" }
    }
}

tasks.check {
    dependsOn(verifyJarContents)
}
