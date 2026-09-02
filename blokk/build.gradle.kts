dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":kregistry"))
    compileOnly(project(":aquatic-common"))

    // Implementations
    compileOnly(libs.itemsadder.api)
    compileOnly(libs.oraxen)
    compileOnly(libs.nexo)

    testImplementation(kotlin("test"))
}
