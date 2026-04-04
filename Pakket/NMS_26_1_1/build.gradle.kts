import io.papermc.paperweight.userdev.ReobfArtifactConfiguration

plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
}

dependencies {
    paperweight.paperDevBundle("26.1.1.build.15-alpha")
    api(project(":API"))
    api("gg.aquatic:KEvent:26.0.5")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("net.kyori:adventure-text-serializer-ansi:4.26.1")
}

kotlin {
    jvmToolchain(25)
}

tasks.matching { it.name == "reobfJar" }.configureEach {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "NMS_26_1_1"
            version = project.version.toString()

            from(components["java"])
        }
    }
}
