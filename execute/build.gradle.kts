dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlinx.coroutines.core)

    api(project(":kregistry"))
    api(project(":kevent"))
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))
    api(project(":aquatic-common"))
}
