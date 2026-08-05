dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    api(project(":kholograms:kholograms-core"))
    api(project(":aquatic-common"))
    api(project(":kregistry"))
    api(project(":execute"))
    api(project(":stacked"))

    testImplementation(kotlin("test"))
}
