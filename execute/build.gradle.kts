dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    api(project(":kregistry"))
    api(project(":kevent"))
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))
    api(project(":aquatic-common"))
}
