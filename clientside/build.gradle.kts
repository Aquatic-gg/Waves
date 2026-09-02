dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlinx.coroutines.core)

    api(project(":aquatic-common"))
    api(project(":execute"))
    api(project(":snapshot-map"))
    api(project(":pakket"))
    api(project(":dispatch"))
    api(project(":kevent"))
    api(project(":blokk"))
    api(project(":kregistry"))

    compileOnly(libs.model.engine)
    compileOnly(libs.bettermodel)

    testImplementation(kotlin("test"))
}
