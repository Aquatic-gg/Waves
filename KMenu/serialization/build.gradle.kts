plugins {
    `java-library`
}

dependencies {
    api(project(":core"))

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    api("gg.aquatic.execute:Execute:26.0.1")
    api("gg.aquatic:Stacked:26.0.2")

    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "KMenu-serialization"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
