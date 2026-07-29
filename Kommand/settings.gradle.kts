plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Kommand"

include(":kommand-core")
include(":kommand-paper")
include(":kommand-velocity")
