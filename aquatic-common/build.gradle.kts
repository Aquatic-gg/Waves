dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlinx.coroutines.core)

    api(libs.slf4j.api)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.hikaricp)
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))

    testImplementation(kotlin("test"))
}
