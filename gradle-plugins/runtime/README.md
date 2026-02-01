# Runtime

[![Reposilite](https://repo.nekroplex.com/api/badge/latest/releases/gg/aquatic/runtime-core?color=40c14a&name=Reposilite)](https://repo.nekroplex.com/#/releases/gg/aquatic/runtime-core)

Runtime is a small toolset for generating a dependency manifest at build time and resolving or relocating those
dependencies at runtime. It ships as:

- runtime-core: Java library used at runtime to download, verify, and relocate dependencies.
- runtime-gradle-plugin: Gradle plugin that generates a dependencies.json manifest and syncs Shadow relocations.

## Features

- Build-time manifest generation based on a dedicated runtimeDownload configuration.
- Optional relocation mapping compatible with Shadow.
- Runtime resolver with checksum verification and support for private repositories via env placeholders.
- Private repository resolving using enviroment variables/files

## Installation

Add the repository for both the Gradle plugin and runtime-core:

```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        maven { url = uri("https://repo.nekroplex.com/releases") }
        gradlePluginPortal()
    }
}
```

```kotlin
// build.gradle.kts
plugins {
    id("gg.aquatic.runtime") version "26.0.4"
    java
}

repositories {
    maven { url = uri("https://repo.nekroplex.com/releases") }
    mavenCentral()
}
```

The plugin automatically adds `gg.aquatic:runtime-core` to `implementation` using the same version as the plugin.
You can override or disable it:

```kotlin
dependencyResolution {
    // addRuntimeCore.set(false)
    // runtimeCoreVersion.set("26.0.2")
}
```

## Gradle plugin usage

Configure repositories, relocations, and runtime dependencies:

```kotlin
dependencyResolution {
    repo("https://repo.maven.apache.org/maven2/")
    repo("https://repo.papermc.io/repository/maven-public/")
    repo("https://repo.mc-cosmos.com/private") {
        credentials($$"${MAVEN_USER}", $$"${MAVEN_PASS}")
    }

    relocate("kotlin", "com.example.libs.kotlin")
    relocate("kotlinx", "com.example.libs.kotlinx")
}

dependencies {
    runtimeDownload("org.jetbrains.kotlin:kotlin-stdlib:2.3.0")
    runtimeDownload("org.jetbrains.kotlin:kotlin-reflect:2.3.0")
    runtimeDownload("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}
```

The plugin generates `dependencies.json` into your build resources and wires it into `processResources`.

## Runtime usage (Paper)

Load the generated manifest and resolve dependencies at runtime:

```java
public class DependencyLoader implements PluginLoader {

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        try {
            Path baseDir = Path.of("plugins/Core/dependencies");

            DependencyManager.create(baseDir)
                    .loadSecrets(Path.of("plugins/Core/.env"))
                    .loadSecrets(Path.of(".env"))
                    .process(getClass().getResourceAsStream("/dependencies.json"), jar -> {
                        classpathBuilder.addLibrary(new JarLibrary(jar));
                    });

            System.out.println("[DependencyLoader] Runtime dependencies loaded successfully.");
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        System.err.println("CRITICAL: Dependency loading failed!");
        e.printStackTrace();
        System.exit(1);
    }
}
```

The `.env` file can contain secrets referenced by `${VAR}` placeholders used in repository credentials.

## Contributing

Issues and PRs are welcome.
