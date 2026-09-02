dependencies {
    api(project(":kommand:kommand-core"))
    compileOnlyApi(libs.velocity.api)

    testImplementation(kotlin("test"))
}
