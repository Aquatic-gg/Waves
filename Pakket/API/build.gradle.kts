group = "gg.aquatic.pakket"

repositories {
    maven("https://repo.nekroplex.com/releases")
    maven("https://mvn.lumine.io/repository/maven-public/")
}

val isMainProject = project.parent == rootProject

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.9.Final")
    if (isMainProject) {
        compileOnly("gg.aquatic:KEvent:1.0.4")
    }

    compileOnly("com.ticxo.modelengine:ModelEngine:R4.0.8")
}