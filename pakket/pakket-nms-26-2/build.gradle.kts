plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("26.2.build.121-stable")

    api(project(":pakket:pakket-api"))
    api(project(":kevent"))
    compileOnly(libs.kotlinx.coroutines.core)
    compileOnly(libs.adventure.text.serializer.ansi)
}

tasks.matching { it.name == "reobfJar" }.configureEach {
    enabled = false
}
