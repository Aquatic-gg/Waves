dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    api(project(":pakket:pakket-api"))
    api(project(":pakket:pakket-nms-1-21-4"))
    api(project(":pakket:pakket-nms-1-21-8"))
    api(project(":pakket:pakket-nms-1-21-9"))
    api(project(mapOf("path" to ":pakket:pakket-nms-26-1-1", "configuration" to "archives")))
    api(project(":kevent"))
    api(project(":aquatic-common"))
}
