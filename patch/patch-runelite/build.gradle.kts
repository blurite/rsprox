plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}

dependencies {
    implementation(projects.patch)
    implementation(rootProject.libs.inline.logger)
    implementation(libs.zip4j)
}
