val exposedVersion = "1.2.0"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api(project(":kregistry"))
    api(project(":kevent"))
    api(project(":aquatic-common"))
    api(project(":dispatch"))
    api(project(":dispatch:dispatch-paper"))

    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1") {
        exclude(group = "org.bukkit", module = "bukkit")
    }

    api("org.jetbrains.exposed:exposed-core:$exposedVersion")
    api("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    api("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    api("com.github.ben-manes.caffeine:caffeine:3.2.3")
    api("redis.clients:jedis:7.4.1")
    api("com.zaxxer:HikariCP:7.0.2")

    testImplementation(kotlin("test"))
}
