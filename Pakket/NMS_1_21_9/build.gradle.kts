plugins {
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.19"
}

group = "gg.aquatic.pakket.nms"

repositories {
    maven("https://repo.nekroplex.com/releases")
}

val rootPrefix = if (project.parent == rootProject) "" else ":${project.parent!!.name}"

dependencies {
    paperweight.paperDevBundle("1.21.9-R0.1-SNAPSHOT")
    compileOnly(project("$rootPrefix:API"))
    compileOnly("gg.aquatic:KEvent:1.0.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}