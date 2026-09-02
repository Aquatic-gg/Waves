dependencies {
    compileOnly(libs.paper.api)

    api(project(":kmenu:kmenu-core"))
    api(project(":execute"))
    api(project(":stacked"))

    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
}
