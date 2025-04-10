dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.rsprot.buffer)
    implementation(projects.protocol)
    implementation(libs.inline.logger)
    implementation(projects.cache)
    implementation(projects.cache.cacheApi)
}
