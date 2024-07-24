dependencies {
    implementation(platform(libs.netty.bom))
    implementation(libs.netty.buffer)
    implementation(libs.bundles.openrs2)
    implementation(libs.bundles.jackson)
    implementation(libs.inline.logger)
    implementation(libs.rsprot.buffer)
    implementation(platform(rootProject.libs.log4j.bom))
    implementation(rootProject.libs.bundles.log4j)
}
