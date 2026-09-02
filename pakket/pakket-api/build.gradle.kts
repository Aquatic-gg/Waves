dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.netty.all)
    compileOnly(libs.model.engine)

    api(project(":kevent"))
    api(project(":aquatic-common"))
    api(project(":blokk"))
    api(project(":stacked"))
}
