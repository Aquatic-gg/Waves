dependencies {
    api(project(":kommand:kommand-core"))
    compileOnlyApi(libs.paper.api)

    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
}
