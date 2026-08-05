dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.12.Final")
    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")

    api(project(":kevent"))
    api(project(":aquatic-common"))
    api(project(":blokk"))
    api(project(":stacked"))
}
