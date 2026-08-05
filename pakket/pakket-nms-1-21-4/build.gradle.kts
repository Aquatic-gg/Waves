plugins {
    id("io.papermc.paperweight.userdev")
}

// The 1.21.4 dev bundle pins codebook 1.0.14, whose bundled ASM cannot read
// class files newer than Java 24. Running its setup on the project toolchain
// (25) makes it fail with "Unsupported class file major version 69", so give
// only that step an older JDK. Compilation still uses the project toolchain.
paperweight {
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    )
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")

    api(project(":pakket:pakket-api"))
    api(project(":kevent"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
