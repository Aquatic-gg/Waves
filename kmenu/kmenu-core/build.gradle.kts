dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlinx.coroutines.core)

    api(project(":pakket"))
    api(project(":aquatic-common"))
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))
    api(project(":replace"))
    api(project(":kevent"))
    api(project(":snapshot-map"))

    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
    testImplementation(libs.kotlinx.coroutines.test)
}
