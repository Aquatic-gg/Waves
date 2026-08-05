pluginManagement {
    repositories {
        mavenLocal()
        maven { url = uri("https://repo.aquatic.gg/releases") }
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Waves"

include(
    ":aquatic-common",
    ":blokk",
    ":clientside",
    ":dispatch",
    ":dispatch:dispatch-paper",
    ":execute",
    ":kevent",
    ":kholograms:kholograms-core",
    ":kholograms:kholograms-serialization",
    ":klocale:klocale-common",
    ":klocale:klocale-paper",
    ":kmenu:kmenu-core",
    ":kmenu:kmenu-serialization",
    ":kommand:kommand-core",
    ":kommand:kommand-paper",
    ":kommand:kommand-velocity",
    ":kregistry",
    ":kurrency",
    ":pakket",
    ":pakket:pakket-api",
    ":pakket:pakket-nms-1-21-4",
    ":pakket:pakket-nms-1-21-8",
    ":pakket:pakket-nms-1-21-9",
    ":pakket:pakket-nms-26-1-1",
    ":quick-mini-message",
    ":replace",
    ":snapshot-map",
    ":stacked",
    ":statistik",
    ":tree-papi",
)
