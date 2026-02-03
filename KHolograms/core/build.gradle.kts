dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("gg.aquatic:Common:26.0.13") {
        isChanging = true
    }
    compileOnly("gg.aquatic.execute:Execute:26.0.1")
    compileOnly("gg.aquatic.replace:Replace:26.0.2")
    compileOnly("gg.aquatic:snapshotmap:26.0.2")
    compileOnly("gg.aquatic:Pakket:26.1.6")
    compileOnly("gg.aquatic:Dispatch:26.0.2")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "KHolograms"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
