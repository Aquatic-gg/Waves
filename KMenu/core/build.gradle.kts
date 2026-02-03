plugins {
    `java-library`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    api("gg.aquatic:Pakket:26.1.7") {
        isTransitive = false
    }
    api("gg.aquatic.pakket:API:26.1.7")
    api("gg.aquatic:Common:26.0.13") {
        isChanging = true
    }
    api("gg.aquatic:Dispatch:26.0.2")
    api("gg.aquatic.replace:Replace:26.0.3")
    api("gg.aquatic:KEvent:26.0.5")
    api("gg.aquatic:snapshotmap:26.0.2")

    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "KMenu"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
