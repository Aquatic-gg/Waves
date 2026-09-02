dependencies {
    compileOnly(libs.paper.api)
    compileOnly(project(":aquatic-common"))
    compileOnly(project(":tree-papi"))
    compileOnly(project(":kregistry"))

    testImplementation(kotlin("test"))
}
