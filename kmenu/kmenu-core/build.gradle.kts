dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    api(project(":pakket"))
    api(project(":aquatic-common"))
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))
    api(project(":replace"))
    api(project(":kevent"))
    api(project(":snapshot-map"))

    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}
