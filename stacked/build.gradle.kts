dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.kotlinx.coroutines.core)
    compileOnly(project(":kregistry"))
    compileOnly(project(":aquatic-common"))
    compileOnly(project(":kevent"))

    // Item adapters
    //compileOnly("gg.aquatic:AEAPI:1.0") //FIXME: Find and add the correct version of AEAPI
    compileOnly(libs.oraxen)
    compileOnly(libs.itemsadder.legacy)
    compileOnly(libs.mythic.dist)
    compileOnly(libs.mythiclib.dist)
    compileOnly(libs.mmoitems.api)
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.eco)
    compileOnly(libs.craft.engine.core)
    compileOnly(libs.craft.engine.bukkit)
    compileOnly(libs.nexo)
    compileOnly(libs.score)

    testImplementation(kotlin("test"))
}
