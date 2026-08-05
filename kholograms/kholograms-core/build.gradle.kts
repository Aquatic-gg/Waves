dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    api(project(":aquatic-common"))
    api(project(":replace"))
    api(project(":snapshot-map"))
    api(project(":pakket")) {
        isTransitive = false
    }
    api(project(":pakket:pakket-api"))
    api(project(":dispatch"))

    testImplementation(kotlin("test"))
}
