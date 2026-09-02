dependencies {
    compileOnly(libs.paper.api)

    api(libs.kotlinx.coroutines.core)
    api(project(":kregistry"))
    api(project(":kevent"))
    api(project(":aquatic-common"))
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))

    compileOnly(libs.vault.api) {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.caffeine)
    api(libs.jedis)
    api(libs.hikaricp)

    testImplementation(kotlin("test"))
}
