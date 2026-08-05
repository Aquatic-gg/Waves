dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    api(project(":aquatic-common"))
    api(project(":execute"))
    api(project(":snapshot-map"))
    api(project(":pakket"))
    api(project(":dispatch"))
    api(project(":kevent"))
    api(project(":blokk"))
    api(project(":kregistry"))

    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.9")
    compileOnly("io.github.toxicity188:bettermodel-bukkit-api:3.0.0")

    testImplementation(kotlin("test"))
}
