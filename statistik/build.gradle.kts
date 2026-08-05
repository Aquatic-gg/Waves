dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":aquatic-common"))
    compileOnly(project(":tree-papi"))
    compileOnly(project(":kregistry"))

    testImplementation(kotlin("test"))
}
