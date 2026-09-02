dependencies {
    compileOnlyApi(libs.brigadier)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
}
