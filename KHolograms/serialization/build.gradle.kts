plugins {
    `java-library`
}

dependencies {
    api(project(":core"))
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("gg.aquatic:Common:26.0.13") {
        isChanging = true
    }
    compileOnly("gg.aquatic:KRegistry:25.0.2")
    compileOnly("gg.aquatic.execute:Execute:26.0.1")
    compileOnly("gg.aquatic:Stacked:26.0.2")
    testImplementation(kotlin("test"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "KHolograms-serialization"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
