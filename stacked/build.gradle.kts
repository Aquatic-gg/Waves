dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    compileOnly(project(":kregistry"))
    compileOnly(project(":aquatic-common"))
    compileOnly(project(":kevent"))

    // Item adapters
    //compileOnly("gg.aquatic:AEAPI:1.0") //FIXME: Find and add the correct version of AEAPI
    compileOnly("io.th0rgal:oraxen:1.211.0")
    compileOnly("com.github.LoneDev6:API-ItemsAdder:3.6.2-beta-r3")
    compileOnly("io.lumine:Mythic-Dist:5.11.2")
    compileOnly("io.lumine:MythicLib-dist:1.6.2-SNAPSHOT")
    compileOnly("net.Indyuce:MMOItems-API:6.9.5-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
    compileOnly("com.willfp:eco:7.1.0")
    compileOnly("net.momirealms:craft-engine-core:0.0.67")
    compileOnly("net.momirealms:craft-engine-bukkit:0.0.67")
    compileOnly("com.nexomc:nexo:1.21.0")
    compileOnly("com.github.Ssomar-Developement:SCore:5.25.3.9")

    testImplementation(kotlin("test"))
}
