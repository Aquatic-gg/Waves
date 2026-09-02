plugins {
    id("com.gradleup.shadow")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.paper.api)
    testRuntimeOnly(libs.junit.platform.launcher)
}
