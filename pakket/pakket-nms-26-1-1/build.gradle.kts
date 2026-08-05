plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("26.1.1.build.15-alpha")

    api(project(":pakket:pakket-api"))
    api(project(":kevent"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly("net.kyori:adventure-text-serializer-ansi:4.26.1")
}

tasks.matching { it.name == "reobfJar" }.configureEach {
    enabled = false
}
