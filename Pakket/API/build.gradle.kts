plugins {
    `maven-publish`
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.10.Final")
    api("gg.aquatic:KEvent:26.0.5")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
    api("gg.aquatic:Common:26.0.16") {
        isChanging = true
    }
    api("gg.aquatic:Blokk:26.0.2")
    api("gg.aquatic:Stacked:26.0.4")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "API"
            version = project.version.toString()

            from(components["java"])
        }
    }
}
