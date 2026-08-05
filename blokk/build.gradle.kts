dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":kregistry"))
    compileOnly(project(":aquatic-common"))

    // Implementations
    compileOnly("dev.lone:api-itemsadder:4.0.10")
    compileOnly("io.th0rgal:oraxen:1.211.0")
    compileOnly("com.nexomc:nexo:1.21.0")

    testImplementation(kotlin("test"))
}
