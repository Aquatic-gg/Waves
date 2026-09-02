dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.placeholderapi)

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.paper.api)
}
