dependencies {
    compileOnly(libs.paper.api)

    api(project(":kholograms:kholograms-core"))
    api(project(":aquatic-common"))
    api(project(":kregistry"))
    api(project(":execute"))
    api(project(":stacked"))

    testImplementation(kotlin("test"))
}
