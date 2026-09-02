dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlinx.coroutines.core)

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
