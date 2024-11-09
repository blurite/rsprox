dependencies {
    implementation(platform(rootProject.libs.netty.bom))
    implementation(rootProject.libs.netty.buffer)
    implementation(rootProject.libs.netty.transport)
    implementation(rootProject.libs.netty.handler)
    implementation(rootProject.libs.netty.codec.http)
    implementation(rootProject.libs.rsprot.crypto)
    implementation(libs.bundles.openrs2) {
        // excluded due to artifact size + not used
        exclude(group = "org.xerial", module = "sqlite-jdbc")
    }
    implementation(libs.bundles.jackson)
    implementation(libs.inline.logger)
    implementation(libs.rsprot.buffer)
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.bundles.log4j)
    implementation(projects.cache.cacheApi)
}
