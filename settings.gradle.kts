plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
rootProject.name = "Waves"

include("API")
include("NMS_1_21_1")
include("NMS_1_21_4")
include("NMS_1_21_5")
include("NMS_1_21_7")
include("NMS_1_21_9")