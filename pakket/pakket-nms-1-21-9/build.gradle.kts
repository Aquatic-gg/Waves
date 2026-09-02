plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")

    api(project(":pakket:pakket-api"))
    api(project(":kevent"))
    compileOnly(libs.kotlinx.coroutines.core)
}
