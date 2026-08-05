val exposedVersion = "1.2.0"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    api("org.slf4j:slf4j-api:2.0.17")
    api("org.jetbrains.exposed:exposed-core:$exposedVersion")
    api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    api("com.zaxxer:HikariCP:7.0.2")
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))

    testImplementation(kotlin("test"))
}
